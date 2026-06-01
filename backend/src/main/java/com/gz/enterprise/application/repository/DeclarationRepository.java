package com.gz.enterprise.application.repository;

import com.gz.enterprise.application.domain.Declaration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationRepository extends JpaRepository<Declaration, Long> {
    List<Declaration> findByEnterpriseId(Long enterpriseId);
    List<Declaration> findByEnterpriseIdAndStatus(Long enterpriseId, String status);
    List<Declaration> findByType(String type);
    List<Declaration> findByStatus(String status);
}
