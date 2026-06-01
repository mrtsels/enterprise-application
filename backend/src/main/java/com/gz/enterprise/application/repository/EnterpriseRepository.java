package com.gz.enterprise.application.repository;

import com.gz.enterprise.application.domain.Enterprise;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Optional<Enterprise> findByUsername(String username);
    Optional<Enterprise> findByName(String name);
}
