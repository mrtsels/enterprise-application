package com.gz.enterprise.application.repository;

import com.gz.enterprise.application.domain.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEnterpriseId(Long enterpriseId);
    List<Document> findByEnterpriseIdAndCategory(Long enterpriseId, String category);
}
