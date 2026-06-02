package com.gz.enterprise.application.service;

import com.gz.enterprise.application.domain.Document;
import com.gz.enterprise.application.repository.DocumentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final DocumentRepository documentRepo;

    @Value("${app.upload.path}")
    private String uploadPath;

    private static final long MAX_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf"};
    private static final String[] ALLOWED_MIME = {"image/jpeg", "image/png", "application/pdf"};

    /**
     * Save uploaded file to disk and create Document record.
     *
     * @return Document entity with fileUrl pointing to the stored file
     */
    public Document store(MultipartFile file, Long enterpriseId, Long declarationId, String category) {
        // Validate
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件大小超过10MB限制");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed";
        }

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot).toLowerCase();
        }

        boolean validExt = false;
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(ext)) { validExt = true; break; }
        }
        if (!validExt) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 JPG/PNG/PDF");
        }

        // Build path: {uploadPath}/{declarationId}/{date}_{originalName}
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uniqueName = dateStr + "_" + System.nanoTime() + ext;

        Path declDir = Paths.get(uploadPath, String.valueOf(declarationId)).normalize();
        try {
            Files.createDirectories(declDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }

        // Path traversal protection
        Path targetPath = declDir.resolve(uniqueName).normalize();
        if (!targetPath.startsWith(declDir)) {
            throw new IllegalArgumentException("无效的文件路径");
        }

        try {
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException e) {
            log.error("Failed to save file: {}", originalName, e);
            throw new RuntimeException("文件保存失败", e);
        }

        // Create Document entity
        Document doc = new Document();
        doc.setEnterpriseId(enterpriseId);
        doc.setName(originalName);
        doc.setFileName(uniqueName);
        doc.setFileUrl(targetPath.toString());
        doc.setFileSize(file.getSize());
        doc.setCategory(category);

        return documentRepo.save(doc);
    }
}
