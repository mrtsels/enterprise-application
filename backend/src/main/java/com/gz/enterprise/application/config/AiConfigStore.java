package com.gz.enterprise.application.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 视觉模型 + 提示词配置的运行时存储（持久化到JSON文件）
 */
@Component
public class AiConfigStore {

    private static final Logger log = LoggerFactory.getLogger(AiConfigStore.class);

    private final DeepSeekProperties props;
    private final String configPath;

    private String baseUrl;
    private String model;
    private String apiKey;
    private final Map<String, String> prompts = new LinkedHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public AiConfigStore(DeepSeekProperties props,
                         @Value("${app.upload.path:./uploads}") String uploadPath) {
        this.props = props;
        this.configPath = uploadPath + "/ai-config.json";
    }

    @PostConstruct
    public void init() {
        // Try loading from persistence file first
        File file = new File(configPath);
        if (file.exists()) {
            try {
                Map<String, Object> saved = mapper.readValue(file,
                        new TypeReference<Map<String, Object>>() {});
                this.baseUrl = str(saved.get("baseUrl"));
                this.model = str(saved.get("model"));
                this.apiKey = str(saved.get("apiKey"));
                Object p = saved.get("prompts");
                if (p instanceof Map) {
                    prompts.clear();
                    ((Map<String, Object>) p).forEach((k, v) -> prompts.put(k, str(v)));
                }
                log.info("Loaded AI config from {}", configPath);
                return;
            } catch (IOException e) {
                log.warn("Failed to load AI config from {}, using defaults", configPath, e);
            }
        }
        // Fall back to application.yml defaults
        this.baseUrl = props.getBaseUrl();
        this.model = props.getModel();
        this.apiKey = props.getApiKey();
        // Default prompts
        prompts.put("营业执照（副本）", String.join("\n",
                "你是一个专业的中国营业执照OCR识别系统。",
                "从图片中提取字段并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段名必须使用以下驼峰命名：",
                "companyName, creditCode, legalRepresentative, registeredCapital,",
                "capitalAmount, capitalUnit, establishedDate, fullAddress,",
                "province, city, district, businessScope, registrationAuthority, validPeriod",
                "establishedDate格式为YYYY-MM-DD。",
                "capitalAmount是纯数字，capitalUnit是人民币单位（如 万元）。",
                "如果字段在图片中不可见或不清晰，设置为null。",
                "province/city/district从fullAddress中解析提取。"));
    }

    /** Persist current config to disk */
    public void saveToFile() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("baseUrl", baseUrl);
        data.put("model", model);
        data.put("apiKey", apiKey);
        data.put("prompts", new LinkedHashMap<>(prompts));
        try {
            File dir = new File(configPath).getParentFile();
            if (!dir.exists()) dir.mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configPath), data);
        } catch (IOException e) {
            log.error("Failed to persist AI config to {}", configPath, e);
        }
    }

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getApiKey() { return apiKey; }
    public Map<String, String> getPrompts() { return prompts; }

    public String getPrompt(String materialName) {
        return prompts.getOrDefault(materialName,
                "请提取这张图片中的所有字段，仅返回JSON。");
    }

    public Map<String, Object> toFullMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("baseUrl", baseUrl);
        m.put("model", model);
        m.put("apiKey", apiKey);
        m.put("prompts", new LinkedHashMap<>(prompts));
        return m;
    }

    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> body) {
        if (body.containsKey("baseUrl")) this.baseUrl = str(body.get("baseUrl"));
        if (body.containsKey("model")) this.model = str(body.get("model"));
        if (body.containsKey("apiKey")) this.apiKey = str(body.get("apiKey"));
        Object p = body.get("prompts");
        if (p instanceof Map) {
            prompts.clear();
            ((Map<String, Object>) p).forEach((k, v) -> prompts.put(k, str(v)));
        }
        saveToFile();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
