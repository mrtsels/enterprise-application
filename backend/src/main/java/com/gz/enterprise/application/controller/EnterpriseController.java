package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.domain.Enterprise;
import com.gz.enterprise.application.service.EnterpriseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @GetMapping
    public List<Enterprise> list(@RequestParam(defaultValue = "") String username) {
        if (!username.isBlank()) {
            return List.of(enterpriseService.getByUsername(username));
        }
        return List.of();
    }

    @GetMapping("/{id}")
    public Enterprise detail(@PathVariable Long id) {
        return enterpriseService.get(id);
    }

    @PostMapping
    public Enterprise create(@RequestBody Enterprise enterprise) {
        return enterpriseService.save(enterprise);
    }
}
