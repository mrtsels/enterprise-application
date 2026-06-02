package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.config.AiConfigStore;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigStore aiConfigStore;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return aiConfigStore.toMap();
    }

    @PutMapping("/config")
    public Map<String, String> updateConfig(@RequestBody Map<String, String> body) {
        aiConfigStore.update(body);
        return aiConfigStore.toMap();
    }
}
