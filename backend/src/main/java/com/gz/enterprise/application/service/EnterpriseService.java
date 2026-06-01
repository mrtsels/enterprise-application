package com.gz.enterprise.application.service;

import com.gz.enterprise.application.domain.Enterprise;
import com.gz.enterprise.application.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EnterpriseService {

    private final EnterpriseRepository enterpriseRepo;

    public Enterprise get(Long id) {
        return enterpriseRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("企业不存在"));
    }

    public Enterprise getByUsername(String username) {
        return enterpriseRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("企业不存在"));
    }

    public Enterprise save(Enterprise enterprise) {
        return enterpriseRepo.save(enterprise);
    }
}
