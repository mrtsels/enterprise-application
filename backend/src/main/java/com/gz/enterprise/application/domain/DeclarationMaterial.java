package com.gz.enterprise.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报材料清单
 * 每份申报对应一份材料清单，记录材料上传状态、AI识别结果等。
 */
@Getter
@Setter
@Entity
@Table(name = "declaration_material")
public class DeclarationMaterial extends BaseEntity {

    @Column(nullable = false)
    private Long declarationId;

    @Column(name = "group_name", length = 64, nullable = false)
    private String materialType; // 基础资质/财务指标/创新能力/经营规范/特色化

    @Column(name = "name", length = 255)
    private String materialName; // 材料名称

    private Long documentId; // 关联文档ID

    @Column(length = 32)
    private String status = "MISSING"; // MISSING / UPLOADED / IDENTIFIED / VERIFIED

    @Column(columnDefinition = "TEXT")
    private String identifyResult; // AI识别结果摘要

    @Column
    private Integer required = 1; // 是否必交 1=必交 0=选交
}
