package com.gz.enterprise.application.service;

import com.gz.enterprise.application.domain.Document;
import com.gz.enterprise.application.repository.DocumentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DocumentService {

    private final DocumentRepository documentRepo;

    public List<Document> listByEnterprise(Long enterpriseId) {
        return documentRepo.findByEnterpriseId(enterpriseId);
    }

    public Document get(Long id) {
        return documentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文档不存在"));
    }

    public byte[] getFileContent(Long id) {
        Document doc = get(id);
        try {
            return Files.readAllBytes(Paths.get(doc.getFileUrl()));
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败", e);
        }
    }

    public Document save(Document document) {
        return documentRepo.save(document);
    }

    public void delete(Long id) {
        documentRepo.deleteById(id);
    }
}
