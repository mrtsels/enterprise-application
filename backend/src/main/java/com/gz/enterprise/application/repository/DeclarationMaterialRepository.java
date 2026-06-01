package com.gz.enterprise.application.repository;

import com.gz.enterprise.application.domain.DeclarationMaterial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationMaterialRepository extends JpaRepository<DeclarationMaterial, Long> {
    List<DeclarationMaterial> findByDeclarationId(Long declarationId);
    long countByDeclarationIdAndStatusNot(Long declarationId, String status);
}
