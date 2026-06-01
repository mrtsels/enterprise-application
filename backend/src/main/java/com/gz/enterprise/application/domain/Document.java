package com.gz.enterprise.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 文档/文件实体
 * 存储企业上传的各类材料文件元信息。
 */
@Getter
@Setter
@Entity
@Table(name = "document")
public class Document extends BaseEntity {

    @NotNull
    private Long enterpriseId;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 64)
    private String category; // 基础资质/财务指标/创新能力/经营规范/特色化/通用

    @Column(length = 512)
    private String fileUrl;

    @Column(length = 255)
    private String fileName;

    private Long fileSize;
}
