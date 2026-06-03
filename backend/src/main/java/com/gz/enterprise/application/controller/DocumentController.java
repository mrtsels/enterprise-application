package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.domain.Document;
import com.gz.enterprise.application.service.DocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/enterprise/{enterpriseId}")
    public List<Document> listByEnterprise(@PathVariable Long enterpriseId) {
        return documentService.listByEnterprise(enterpriseId);
    }

    @GetMapping("/{id}")
    public Document detail(@PathVariable Long id) {
        return documentService.get(id);
    }

    @PostMapping
    public Document create(@RequestBody Document document) {
        return documentService.save(document);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        byte[] content = documentService.getFileContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
