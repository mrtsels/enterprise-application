package com.gz.enterprise.application.controller;

import com.gz.enterprise.application.domain.Declaration;
import com.gz.enterprise.application.domain.DeclarationMaterial;
import com.gz.enterprise.application.service.DeclarationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 申报项目管理 — 企业提交申报材料 & 填写申报项目
 */
@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DeclarationController {

    private final DeclarationService declarationService;

    // ==================== 申报项目 ====================

    @GetMapping
    public Page<Declaration> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return declarationService.list(keyword, status, type, page, size);
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public List<Declaration> listByEnterprise(@PathVariable Long enterpriseId) {
        return declarationService.listByEnterprise(enterpriseId);
    }

    @GetMapping("/{id}")
    public Declaration detail(@PathVariable Long id) {
        return declarationService.get(id);
    }

    @PostMapping
    public Declaration create(@Valid @RequestBody Declaration request) {
        return declarationService.save(request);
    }

    @PutMapping("/{id}")
    public Declaration update(@PathVariable Long id, @Valid @RequestBody Declaration request) {
        return declarationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        declarationService.delete(id);
    }

    // ==================== 工作流 ====================

    @PostMapping("/{id}/submit")
    public Declaration submit(@PathVariable Long id) {
        return declarationService.submit(id);
    }

    @PostMapping("/{id}/review")
    public Declaration review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return declarationService.review(id, body.get("status"), body.get("reviewComment"));
    }

    @PostMapping("/{id}/ai-evaluate")
    public Declaration aiEvaluate(@PathVariable Long id) {
        return declarationService.aiEvaluate(id);
    }

    // ==================== 申报材料 ====================

    @GetMapping("/{id}/materials")
    public List<DeclarationMaterial> listMaterials(@PathVariable Long id) {
        return declarationService.listMaterials(id);
    }

    @PostMapping("/{id}/materials")
    public DeclarationMaterial addMaterial(@PathVariable Long id, @RequestBody DeclarationMaterial material) {
        return declarationService.addMaterial(id, material);
    }

    @PutMapping("/materials/{materialId}")
    public DeclarationMaterial updateMaterial(@PathVariable Long materialId, @RequestBody DeclarationMaterial request) {
        return declarationService.updateMaterial(materialId, request);
    }

    @DeleteMapping("/materials/{materialId}")
    public void deleteMaterial(@PathVariable Long materialId) {
        declarationService.deleteMaterial(materialId);
    }

    // ==================== 统计 ====================

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return declarationService.getStats();
    }
}
