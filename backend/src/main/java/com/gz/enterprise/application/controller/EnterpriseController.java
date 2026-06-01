package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.domain.Enterprise;
import com.gz.enterprise.application.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @GetMapping("/{id}")
    public Enterprise detail(@PathVariable Long id) {
        return enterpriseService.get(id);
    }

    @PostMapping
    public Enterprise create(@RequestBody Enterprise enterprise) {
        return enterpriseService.save(enterprise);
    }
}
