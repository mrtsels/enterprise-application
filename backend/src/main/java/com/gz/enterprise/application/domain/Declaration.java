package com.gz.enterprise.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报项目主表
 * 企业提交专精特新认定申报的核心实体，包含企业填报信息、AI评分结果、审核状态等。
 */
@Getter
@Setter
@Entity
@Table(name = "declaration")
public class Declaration extends BaseEntity {

    // ===== 基本信息 =====

    @NotNull
    private Long enterpriseId;

    @Column(length = 128)
    private String enterpriseName;

    @NotBlank
    @Column(nullable = false, length = 32)
    private String type; // INNOVATIVE / PROVINCIAL / XIAOJUREN

    @Column(length = 32)
    private String level; // 国家级 / 省级 / 市级

    @Column(length = 32)
    private String category; // 认定 / 复核 / 重新申报

    @Column
    private Integer year;

    @Column(nullable = false, length = 32)
    private String status = "DRAFT"; // DRAFT / SUBMITTED / PENDING / APPROVED / REJECTED

    private LocalDateTime submitTime;

    @Column(length = 32)
    private String declarationCode; // 申报编号 如 DECL-2026-001

    // ===== 核心财务指标 =====

    private Long revenue;                   // 营业收入（万元）
    private Long rdInvestment;              // 研发投入（万元）

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double rdRatio;                 // 研发投入比例（%）

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double debtRatio;               // 资产负债率（%）

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer patentCount;            // 发明专利数

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double marketShare;             // 市场占有率（%）

    @Column(length = 128)
    private String productName;             // 主导产品名称

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private Double productMarketShare;      // 主导产品市场占有率（%）

    @Column(columnDefinition = "TEXT")
    private String chainDescription;        // 产业链配套说明

    // ===== 材料完整度 =====

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer materialCompleteness = 0; // 材料完整度 0-100

    // ===== AI评估结果 =====

    @Column(columnDefinition = "TEXT")
    private String aiSuggestions;           // AI建议 JSON

    @Column(columnDefinition = "TEXT")
    private String aiIssues;                // AI问题 JSON

    @Column(columnDefinition = "TEXT")
    private String aiPassedItems;           // AI预审通过项 JSON

    @Column(columnDefinition = "TEXT")
    private String aiMissingItems;          // AI预审需补充项 JSON

    // ===== 审核信息 =====

    @Column(length = 512)
    private String reviewComment;

    private Long reviewerId;
    private LocalDateTime reviewTime;

    // ===== 专精特新认定标准字段 =====

    // 基本条件
    private Integer marketYears;                    // 从事特定细分市场年限
    private Double equityFinancing2y;               // 近2年股权融资(万元)
    private Double netProfit;                       // 净利润(万元)
    private Double mainRevenue;                     // 主营业务收入(万元)

    // 专业化指标
    private Double mainRevenueRatio;                // 主营业务收入占比(%)
    private Double revenueGrowth2yAvg;              // 近2年营收平均增长率(%)
    private Integer marketYearsScore;               // 细分市场年限得分
    private String leadProductDomain;               // 主导产品所属领域

    // 精细化指标
    private Integer digitalLevel;                   // 数字化水平等级
    private Double netProfitRate;                   // 净利润率(%)
    private Boolean qualityAwardProvincial;         // 省级以上质量奖
    private Boolean qualityIso;                     // ISO9001认证
    private Boolean qualityBrand;                   // 自有品牌
    private Boolean qualityStandard;                // 参与标准制定
    private Integer digitalScore;                   // 数字化得分
    private Integer qualityScore;                   // 质量管理得分
    private Integer refinedScore;                   // 精细化指标总分

    // 特色化指标
    private Boolean featureKeyIndustry;             // 重点领域
    private Boolean featureSpecialTech;             // 特色工艺技术
    private Boolean featureFirstSet;                // 首台套
    private Boolean featureGreenMfg;                // 绿色制造
    private Boolean featureChuangke;                // 创客广东
    private Boolean featureDesignAward;             // 省长杯设计奖
    private Boolean talentNational;                 // 国家级人才
    private Boolean talentProvincial;               // 省级人才
    private Boolean talentCity;                     // 市级人才
    private Boolean talentRecommend;                // 人才推荐
    private Integer featureScore;                   // 特色化指标得分

    // 创新能力指标
    private Integer ipHighValue;                    // Ⅰ类高价值知识产权
    private Integer ipClass1Self;                   // 自主研发Ⅰ类知识产权
    private Integer ipClass1;                       // Ⅰ类知识产权
    private Integer ipClass2;                       // Ⅱ类知识产权
    private Double rdStaffRatio;                    // 研发人员占比(%)
    private String rdInstitutionLevel;              // 研发机构级别
    private Integer ipScore;                        // 知识产权得分
    private Integer rdScore;                        // 研发投入得分
    private Integer innovationScoreDetail;          // 创新能力详细得分

    // 直通条件
    private Boolean directTechAward;                // 省级科技奖
    private Boolean directNationalAward;            // 国家级科技奖
    private Boolean directChuangke;                 // 创客中国500强

    // 综合评分
    private Integer totalScore;                     // 评价总分
    private Integer professionalScore;              // 专业化指标得分
    private Boolean declarationEligible;            // 是否符合申报条件
    private String eligibleReason;                  // 条件说明

    @Column(columnDefinition = "MEDIUMTEXT")
    private String formData;                        // 完整表单JSON数据

    private String creditCode;                      // 统一社会信用代码
    private String legalRepresentative;             // 法定代表人
    private String estDate;                         // 注册时间
    private String fullAddress;                     // 通讯地址
    private Double registeredCapital;               // 注册资本（万元）
    private String province;
    private String city;
    private String district;
    private String contactPerson;                   // 联系人
    private String contactPhone;                    // 电话
    private String contactMobile;                   // 手机
    private String email;
    private Long employees;                         // 全职员工数量
    private Long rdStaffCount;                      // 研发人员数量
}
