package com.gz.enterprise.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gz.enterprise.application.config.AiConfigStore;
import com.gz.enterprise.application.config.DeepSeekProperties;
import com.gz.enterprise.application.domain.Document;
import com.gz.enterprise.application.repository.DeclarationMaterialRepository;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR service using DeepSeek Vision API (OpenAI-compatible).
 * Extracts business license fields as structured JSON.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {

    private final DeepSeekProperties props;
    private final AiConfigStore aiConfigStore;
    private final DeclarationMaterialRepository materialRepo;
    private final FileStorageService fileStorageService;
    private final RestClient.Builder restClientBuilder;

    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    private RestClient aiClient() {
        String baseUrl = aiConfigStore.getBaseUrl();
        String apiKey = aiConfigStore.getApiKey();
        return restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Recognize business license fields from uploaded image.
     *
     * @param file          uploaded image file
     * @param declarationId declaration ID
     * @param materialId    declaration material record ID
     * @return extracted fields as Map
     */
    public Map<String, Object> recognize(MultipartFile file, Long declarationId, Long materialId, Long enterpriseId, String materialName) {
        // === Validate file ===
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件大小超过10MB限制");
        }

        // === Save file and create Document ===
        Document doc = fileStorageService.store(file, enterpriseId, declarationId, "基础资质");

        // === Encode image to Base64 ===
        String base64Image;
        String mediaType = getMediaType(file.getOriginalFilename());
        try {
            base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }

        // === Build OpenAI-compatible request ===
        String systemPrompt = aiConfigStore.getPrompt(materialName);
        Map<String, Object> requestBody = buildRequestBody(base64Image, mediaType, systemPrompt);

        // === Call DeepSeek API ===
        String responseBody;
        try {
            responseBody = aiClient().post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("DeepSeek API call failed for materialId={}: {}", materialId, e.getMessage());
            throw new RuntimeException("营业执照识别服务暂时不可用，请稍后重试", e);
        }

        // === Parse response ===
        Map<String, Object> ocrResult = parseResponse(responseBody);

        // === Persist result to DeclarationMaterial ===
        try {
            String identifyJson = new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ocrResult);
            materialRepo.findById(materialId).ifPresent(mat -> {
                mat.setIdentifyResult(identifyJson);
                mat.setStatus("IDENTIFIED");
                mat.setDocumentId(doc.getId());
                materialRepo.save(mat);
            });
        } catch (Exception e) {
            log.warn("Failed to persist OCR result for materialId={}", materialId, e);
        }

        log.info("OCR completed for declarationId={}, materialId={}, fields={}", declarationId, materialId, ocrResult.keySet());
        return ocrResult;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildRequestBody(String base64Image, String mediaType, String systemPrompt) {
        String dataUrl = "data:" + mediaType + ";base64," + base64Image;

        return Map.of(
                "model", aiConfigStore.getModel(),
                "max_tokens", props.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text",
                                        "请提取这张中国营业执照图片中的所有字段，仅返回JSON。"),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", dataUrl))
                        ))
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);

            // OpenAI format: choices[0].message.content
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                log.error("Unexpected API response: {}", responseBody);
                throw new IllegalArgumentException("OCR响应异常：缺少choices");
            }

            String text = choices.get(0).path("message").path("content").asText();
            if (text == null || text.isBlank()) {
                log.error("Empty content in API response: {}", responseBody);
                throw new IllegalArgumentException("OCR响应异常：内容为空");
            }

            // Remove markdown code block wrapping if present
            text = text.trim();
            if (text.startsWith("```")) {
                text = text.replaceAll("```[a-zA-Z]*\\s*", "").replaceAll("```\\s*$", "").trim();
            }

            return mapper.readValue(text, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response as JSON: {}", responseBody, e);
            throw new IllegalArgumentException("OCR识别结果解析失败，请重试", e);
        }
    }

    private String getMediaType(String filename) {
        if (filename == null) return "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "image/jpeg";
    }
}
