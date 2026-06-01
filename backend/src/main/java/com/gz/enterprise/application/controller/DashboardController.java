package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.service.DeclarationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DashboardController {

    private final DeclarationService declarationService;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return declarationService.getStats();
    }
}
