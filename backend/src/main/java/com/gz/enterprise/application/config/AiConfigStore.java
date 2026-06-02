package com.gz.enterprise.application.config;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * AI 视觉模型配置的运行时存储（可被管理端UI实时修改）
 */
@Component
public class AiConfigStore {

    private final DeepSeekProperties props;

    private String baseUrl;
    private String model;
    private String apiKey;

    public AiConfigStore(DeepSeekProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        this.baseUrl = props.getBaseUrl();
        this.model = props.getModel();
        this.apiKey = props.getApiKey();
    }

    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getApiKey() { return apiKey; }

    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setModel(String model) { this.model = model; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public Map<String, String> toMap() {
        return Map.of("baseUrl", baseUrl, "model", model, "apiKey", apiKey);
    }

    public void update(Map<String, String> m) {
        if (m.containsKey("baseUrl")) this.baseUrl = m.get("baseUrl");
        if (m.containsKey("model")) this.model = m.get("model");
        if (m.containsKey("apiKey")) this.apiKey = m.get("apiKey");
    }
}
