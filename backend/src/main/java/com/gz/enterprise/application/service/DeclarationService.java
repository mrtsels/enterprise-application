package com.gz.enterprise.application.service;

import com.gz.enterprise.application.domain.Declaration;
import com.gz.enterprise.application.domain.DeclarationMaterial;
import com.gz.enterprise.application.domain.Document;
import com.gz.enterprise.application.repository.DeclarationMaterialRepository;
import com.gz.enterprise.application.repository.DeclarationRepository;
import com.gz.enterprise.application.service.OcrService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DeclarationService {

    private final DeclarationRepository declarationRepo;
    private final DeclarationMaterialRepository materialRepo;
    private final OcrService ocrService;
    private final FileStorageService fileStorageService;

    // ==================== 申报项目 CRUD ====================

    /**
     * 分页查询申报列表，支持关键字搜索、状态筛选、类型筛选
     */
    public Page<Declaration> list(String keyword, String status, String type, int page, int size) {
        List<Declaration> items = declarationRepo.findAll().stream()
                .filter(item -> keyword == null || keyword.isBlank()
                        || containsIgnoreCase(item.getEnterpriseName(), keyword))
                .filter(item -> status == null || status.isBlank() || status.equals(item.getStatus()))
                .filter(item -> type == null || type.isBlank() || type.equals(item.getType()))
                .sorted(Comparator.comparing(Declaration::getId).reversed())
                .toList();
        return toPage(items, page, size);
    }

    /**
     * 获取企业自己的申报列表
     */
    public List<Declaration> listByEnterprise(Long enterpriseId) {
        return declarationRepo.findByEnterpriseId(enterpriseId).stream()
                .sorted(Comparator.comparing(Declaration::getId).reversed())
                .toList();
    }

    /**
     * 获取申报详情
     */
    public Declaration get(Long id) {
        return declarationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("申报项目不存在"));
    }

    /**
     * 创建或保存申报项目
     */
    @Transactional
    public Declaration save(Declaration request) {
        if (request.getSubmitTime() == null) {
            request.setSubmitTime(java.time.LocalDateTime.now());
        }
        if (request.getYear() == null) {
            request.setYear(request.getSubmitTime().getYear());
        }
        if (request.getLevel() == null) {
            request.setLevel(autoLevel(request.getType()));
        }
        if (request.getCategory() == null) {
            request.setCategory("认定");
        }
        Declaration saved = declarationRepo.save(request);
        // 首次创建时自动初始化材料清单
        if (materialRepo.findByDeclarationId(saved.getId()).isEmpty()) {
            initMaterials(saved);
        }
        return saved;
    }

    private String autoLevel(String type) {
        return switch (type) {
            case "XIAOJUREN" -> "国家级";
            case "PROVINCIAL" -> "省级";
            case "INNOVATIVE" -> "省级";
            default -> "省级";
        };
    }

    /**
     * 提交申报（草稿→待审核，触发AI评估）
     */
    @Transactional
    public Declaration submit(Long id) {
        Declaration entity = get(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new IllegalArgumentException("非草稿状态，无法提交");
        }
        entity.setStatus("PENDING");
        entity.setSubmitTime(java.time.LocalDateTime.now());
        // 自动生成申报编号
        if (entity.getDeclarationCode() == null) {
            String year = String.valueOf(entity.getYear() != null ? entity.getYear() : java.time.Year.now().getValue());
            long count = declarationRepo.count() + 1;
            entity.setDeclarationCode("DECL-" + year + "-" + String.format("%03d", count));
        }
        // AI智能评估
        runAiEvaluation(entity);
        return declarationRepo.save(entity);
    }

    /**
     * 手动触发AI评估
     */
    @Transactional
    public Declaration aiEvaluate(Long id) {
        Declaration entity = get(id);
        runAiEvaluation(entity);
        return declarationRepo.save(entity);
    }

    // ==================== AI评分引擎 ====================

    private void runAiEvaluation(Declaration d) {
        // 四维度评分
        int professionalScore = calculateProfessionalScore(d);   // 专业化 (25分)
        int refinedScore = calculateRefinedScore(d);             // 精细化 (25分)
        int featureScore = calculateFeatureScore(d);             // 特色化 (15分)
        int innovationScoreDetail = calculateInnovationScore(d); // 创新能力 (35分)

        int total = professionalScore + refinedScore + featureScore + innovationScoreDetail;
        d.setProfessionalScore(professionalScore);
        d.setRefinedScore(refinedScore);
        d.setFeatureScore(featureScore);
        d.setInnovationScoreDetail(innovationScoreDetail);
        d.setTotalScore(total);

        // 判断申报条件
        boolean eligible = checkEligibility(d);
        d.setDeclarationEligible(eligible);
        d.setEligibleReason(eligible
                ? "基本条件满足，评价得分" + total + "分，符合申报条件"
                : "基本条件未全部满足或评价得分低于70分");

        // 材料完整度
        var mats = materialRepo.findByDeclarationId(d.getId());
        long totalMats = mats.size();
        long uploaded = mats.stream().filter(m -> !"MISSING".equals(m.getStatus())).count();
        d.setMaterialCompleteness(totalMats > 0 ? (int) (uploaded * 100 / totalMats) : 0);

        // 生成建议、问题、通过项、缺失项
        var suggestions = new java.util.ArrayList<String>();
        var issues = new java.util.ArrayList<Map<String, String>>();
        var passed = new java.util.ArrayList<String>();
        var missing = new java.util.ArrayList<String>();

        if (total >= 90) {
            suggestions.add("继续保持研发投入强度");
            suggestions.add("建议加强知识产权海外布局");
            suggestions.add("可考虑申报更高梯度认定");
        } else if (total >= 60) {
            suggestions.add("建议补充近两年研发费用专项审计报告");
            suggestions.add("建议完善知识产权布局");
            suggestions.add("建议补充市场占有率第三方证明");
            if (d.getRdRatio() != null && d.getRdRatio() < 5)
                issues.add(Map.of("type", "warn", "text", "研发费用占比略低，建议提升研发投入"));
        } else {
            suggestions.add("基本条件存在缺项，建议对照标准自查整改");
            suggestions.add("财务指标未达要求，需提升营收或研发投入");
            suggestions.add("创新能力不足，建议加大专利申请力度");
            if (d.getRevenue() != null && d.getRevenue() < 2000)
                issues.add(Map.of("type", "error", "text", "营业收入低于申报门槛"));
            if (d.getRdRatio() != null && d.getRdRatio() < 3)
                issues.add(Map.of("type", "error", "text", "研发投入占比不足"));
        }

        suggestions.add("专精特新评分: 专业化" + professionalScore + "分, 精细化" + refinedScore
                + "分, 特色化" + featureScore + "分, 创新能力" + innovationScoreDetail + "分");
        suggestions.add(total >= 70 ? "评价得分已达70分，符合申报基本条件" : "评价得分未达70分，建议提升各项指标");

        // 字段填报检查
        if (d.getRevenue() != null) passed.add("年营业收入已填报"); else missing.add("年营业收入未填报");
        if (d.getRdRatio() != null && d.getRdRatio() > 0) passed.add("研发投入占比已填报"); else missing.add("研发投入占比未填报");
        if (d.getPatentCount() != null && d.getPatentCount() > 0) passed.add("发明专利数量已填报"); else missing.add("发明专利数量未填报");
        if (d.getDebtRatio() != null) passed.add("资产负债率已填报"); else missing.add("资产负债率未填报");
        if (d.getProductName() != null && !d.getProductName().isBlank()) passed.add("主导产品名称已填报"); else missing.add("主导产品名称未填报");
        if (d.getChainDescription() != null && !d.getChainDescription().isBlank()) passed.add("产业链配套说明已填报"); else missing.add("产业链配套说明未填报");

        // 材料检查
        for (DeclarationMaterial m : mats) {
            if (m.getRequired() != null && m.getRequired() == 1 && "MISSING".equals(m.getStatus())) {
                missing.add(m.getMaterialName() + "（必交材料未上传）");
            }
        }
        if (d.getMaterialCompleteness() >= 80) passed.add("材料完整度良好（" + d.getMaterialCompleteness() + "%）");
        else missing.add("材料完整度不足（" + d.getMaterialCompleteness() + "%），建议补充上传");

        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            d.setAiSuggestions(mapper.writeValueAsString(suggestions));
            d.setAiIssues(mapper.writeValueAsString(issues));
            d.setAiPassedItems(mapper.writeValueAsString(passed));
            d.setAiMissingItems(mapper.writeValueAsString(missing));
        } catch (Exception e) {
            d.setAiSuggestions("[]");
            d.setAiIssues("[]");
            d.setAiPassedItems("[]");
            d.setAiMissingItems("[]");
        }
    }

    /** 专业化指标评分 (满分25分) */
    private int calculateProfessionalScore(Declaration d) {
        int score = 0;
        Integer marketYears = d.getMarketYears();
        if (marketYears != null) {
            if (marketYears >= 10) score += 5;
            else if (marketYears >= 5) score += 4;
            else if (marketYears >= 3) score += 3;
            else if (marketYears >= 2) score += 2;
        }
        d.setMarketYearsScore(Math.min(score, 5));

        Double mainRatio = d.getMainRevenueRatio();
        if (mainRatio != null) {
            if (mainRatio >= 95) score += 5;
            else if (mainRatio >= 90) score += 4;
            else if (mainRatio >= 80) score += 3;
        }
        Double growth = d.getRevenueGrowth2yAvg();
        if (growth != null) {
            if (growth >= 20) score += 10;
            else if (growth >= 15) score += 8;
            else if (growth >= 10) score += 6;
            else if (growth >= 5) score += 3;
        }
        String domain = d.getLeadProductDomain();
        if (domain != null && !domain.isBlank()) {
            if (domain.contains("补短板") || domain.contains("锻长板") || domain.contains("填空白")) score += 5;
            else if (domain.contains("六基")) score += 4;
            else score += 2;
        }
        return Math.min(score, 25);
    }

    /** 精细化指标评分 (满分25分) */
    private int calculateRefinedScore(Declaration d) {
        int score = 0;
        Integer digitalLevel = d.getDigitalLevel();
        if (digitalLevel != null) {
            if (digitalLevel >= 3) score += 5;
            else if (digitalLevel == 2) score += 3;
            else if (digitalLevel == 1) score += 1;
        }
        d.setDigitalScore(Math.min(score, 5));

        int qualityCount = 0;
        if (Boolean.TRUE.equals(d.getQualityAwardProvincial())) qualityCount++;
        if (Boolean.TRUE.equals(d.getQualityIso())) qualityCount++;
        if (Boolean.TRUE.equals(d.getQualityBrand())) qualityCount++;
        if (Boolean.TRUE.equals(d.getQualityStandard())) qualityCount++;
        int qualityScore = Math.min(qualityCount * 2, 5);
        score += qualityScore;
        d.setQualityScore(qualityScore);

        Double debt = d.getDebtRatio();
        if (debt != null) {
            if (debt <= 40) score += 5;
            else if (debt <= 50) score += 4;
            else if (debt <= 60) score += 3;
            else if (debt <= 70) score += 1;
        }
        Double netProfitRate = d.getNetProfitRate();
        if (netProfitRate != null) {
            if (netProfitRate >= 20) score += 10;
            else if (netProfitRate >= 15) score += 8;
            else if (netProfitRate >= 10) score += 6;
            else if (netProfitRate >= 5) score += 3;
        }
        return Math.min(score, 25);
    }

    /** 特色化指标评分 (满分15分) */
    private int calculateFeatureScore(Declaration d) {
        int score = 0;
        if (Boolean.TRUE.equals(d.getFeatureKeyIndustry())) score += 3;
        if (Boolean.TRUE.equals(d.getFeatureSpecialTech())) score += 2;
        if (Boolean.TRUE.equals(d.getFeatureFirstSet())) score += 3;
        if (Boolean.TRUE.equals(d.getFeatureGreenMfg())) score += 2;
        if (Boolean.TRUE.equals(d.getFeatureChuangke())) score += 2;
        if (Boolean.TRUE.equals(d.getFeatureDesignAward())) score += 1;
        if (Boolean.TRUE.equals(d.getTalentNational())) score += 5;
        else if (Boolean.TRUE.equals(d.getTalentProvincial())) score += 3;
        else if (Boolean.TRUE.equals(d.getTalentCity())) score += 2;
        else if (Boolean.TRUE.equals(d.getTalentRecommend())) score += 1;
        return Math.min(score, 15);
    }

    /** 创新能力指标评分 (满分35分) */
    private int calculateInnovationScore(Declaration d) {
        int score = 0;
        int ipScore = 0;
        if (d.getIpHighValue() != null && d.getIpHighValue() > 0) ipScore += Math.min(d.getIpHighValue() * 5, 10);
        if (d.getIpClass1Self() != null && d.getIpClass1Self() > 0) ipScore += Math.min(d.getIpClass1Self() * 4, 8);
        if (d.getIpClass1() != null && d.getIpClass1() > 0) ipScore += Math.min(d.getIpClass1() * 3, 6);
        if (d.getIpClass2() != null && d.getIpClass2() > 0) ipScore += Math.min(d.getIpClass2() * 1, 4);
        d.setIpScore(Math.min(ipScore, 20));
        score += Math.min(ipScore, 20);

        Long rd = d.getRdInvestment();
        Double rdRatio = d.getRdRatio();
        int rdScore = 0;
        if (rd != null && rdRatio != null) {
            if (rd >= 800 && rdRatio >= 5) rdScore = 10;
            else if (rd >= 500 && rdRatio >= 4) rdScore = 8;
            else if (rd >= 300 && rdRatio >= 3) rdScore = 6;
            else if (rd >= 100 && rdRatio >= 3) rdScore = 4;
            else if (rd >= 50) rdScore = 2;
        }
        d.setRdScore(rdScore);
        score += rdScore;

        Double rdStaff = d.getRdStaffRatio();
        if (rdStaff != null) {
            if (rdStaff >= 30) score += 5;
            else if (rdStaff >= 20) score += 4;
            else if (rdStaff >= 10) score += 3;
            else if (rdStaff >= 5) score += 2;
        }
        return Math.min(score, 35);
    }

    /** 判断是否符合申报条件 */
    private boolean checkEligibility(Declaration d) {
        if (Boolean.TRUE.equals(d.getDirectTechAward()) ||
            Boolean.TRUE.equals(d.getDirectNationalAward()) ||
            Boolean.TRUE.equals(d.getDirectChuangke())) {
            return true;
        }
        boolean basic1 = d.getMarketYears() != null && d.getMarketYears() >= 2;
        boolean basic2 = d.getRdInvestment() != null && d.getRdInvestment() >= 100
                && d.getRdRatio() != null && d.getRdRatio() >= 3.0;
        boolean basic3 = (d.getRevenue() != null && d.getRevenue() >= 1000)
                || (d.getEquityFinancing2y() != null && d.getEquityFinancing2y() >= 2000);
        boolean basic4 = d.getTotalScore() != null && d.getTotalScore() >= 70;
        return basic1 && basic2 && basic3 && basic4;
    }

    // ==================== 材料清单初始化 ====================

    private void initMaterials(Declaration d) {
        var list = new java.util.ArrayList<DeclarationMaterial>();
        list.add(m("基础资质", "营业执照（副本）", true));
        list.add(m("基础资质", "中小企业划型认定证明", true));
        list.add(m("基础资质", "企业章程（最新版）", true));
        list.add(m("基础资质", "法定代表人身份证明", true));
        list.add(m("财务指标", "近两年财务审计报告", true));
        list.add(m("财务指标", "近三年研发费用专项审计报告", true));
        list.add(m("财务指标", "主营业务收入明细表", true));
        list.add(m("财务指标", "资产负债表（近三年）", false));
        list.add(m("创新能力", "知识产权证书", true));
        list.add(m("创新能力", "研发机构证明材料", true));
        list.add(m("创新能力", "研发人员花名册及社保证明", false));
        list.add(m("创新能力", "科技成果转化情况说明", false));
        list.add(m("经营规范", "管理体系认证证书", true));
        list.add(m("经营规范", "数字化水平自测结果截图", true));
        list.add(m("经营规范", "信息化系统建设说明", false));
        list.add(m("经营规范", "无违法记录承诺函", true));
        list.add(m("经营规范", "近两年无重大安全/环保事故证明", true));
        list.add(m("特色化", "自有品牌证明及市场占有率报告", true));
        list.add(m("特色化", "主导产品细分市场排名证明", false));
        if ("XIAOJUREN".equals(d.getType())) {
            list.add(m("特色化", "国家级小巨人补充材料", true));
        }
        for (DeclarationMaterial mat : list) {
            mat.setDeclarationId(d.getId());
            mat.setStatus("MISSING");
            materialRepo.save(mat);
        }
    }

    private DeclarationMaterial m(String type, String name, boolean required) {
        DeclarationMaterial mat = new DeclarationMaterial();
        mat.setMaterialType(type);
        mat.setMaterialName(name);
        mat.setRequired(required ? 1 : 0);
        return mat;
    }

    // ==================== 更新和删除 ====================

    @Transactional
    public Declaration update(Long id, Declaration request) {
        Declaration entity = get(id);
        if (request.getEnterpriseId() != null) entity.setEnterpriseId(request.getEnterpriseId());
        if (request.getEnterpriseName() != null) entity.setEnterpriseName(request.getEnterpriseName());
        if (request.getType() != null) { entity.setType(request.getType()); entity.setLevel(autoLevel(request.getType())); }
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getLevel() != null) entity.setLevel(request.getLevel());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getRevenue() != null) entity.setRevenue(request.getRevenue());
        if (request.getRdInvestment() != null) entity.setRdInvestment(request.getRdInvestment());
        if (request.getRdRatio() != null) entity.setRdRatio(request.getRdRatio());
        if (request.getDebtRatio() != null) entity.setDebtRatio(request.getDebtRatio());
        if (request.getPatentCount() != null) entity.setPatentCount(request.getPatentCount());
        if (request.getMarketShare() != null) entity.setMarketShare(request.getMarketShare());
        if (request.getProductName() != null) entity.setProductName(request.getProductName());
        if (request.getProductMarketShare() != null) entity.setProductMarketShare(request.getProductMarketShare());
        if (request.getChainDescription() != null) entity.setChainDescription(request.getChainDescription());

        if (request.getMarketYears() != null) entity.setMarketYears(request.getMarketYears());
        if (request.getEquityFinancing2y() != null) entity.setEquityFinancing2y(request.getEquityFinancing2y());
        if (request.getNetProfit() != null) entity.setNetProfit(request.getNetProfit());
        if (request.getMainRevenue() != null) entity.setMainRevenue(request.getMainRevenue());
        if (request.getMainRevenueRatio() != null) entity.setMainRevenueRatio(request.getMainRevenueRatio());
        if (request.getRevenueGrowth2yAvg() != null) entity.setRevenueGrowth2yAvg(request.getRevenueGrowth2yAvg());
        if (request.getLeadProductDomain() != null) entity.setLeadProductDomain(request.getLeadProductDomain());
        if (request.getDigitalLevel() != null) entity.setDigitalLevel(request.getDigitalLevel());
        if (request.getNetProfitRate() != null) entity.setNetProfitRate(request.getNetProfitRate());
        if (request.getQualityAwardProvincial() != null) entity.setQualityAwardProvincial(request.getQualityAwardProvincial());
        if (request.getQualityIso() != null) entity.setQualityIso(request.getQualityIso());
        if (request.getQualityBrand() != null) entity.setQualityBrand(request.getQualityBrand());
        if (request.getQualityStandard() != null) entity.setQualityStandard(request.getQualityStandard());
        if (request.getFeatureKeyIndustry() != null) entity.setFeatureKeyIndustry(request.getFeatureKeyIndustry());
        if (request.getFeatureSpecialTech() != null) entity.setFeatureSpecialTech(request.getFeatureSpecialTech());
        if (request.getFeatureFirstSet() != null) entity.setFeatureFirstSet(request.getFeatureFirstSet());
        if (request.getFeatureGreenMfg() != null) entity.setFeatureGreenMfg(request.getFeatureGreenMfg());
        if (request.getFeatureChuangke() != null) entity.setFeatureChuangke(request.getFeatureChuangke());
        if (request.getFeatureDesignAward() != null) entity.setFeatureDesignAward(request.getFeatureDesignAward());
        if (request.getTalentNational() != null) entity.setTalentNational(request.getTalentNational());
        if (request.getTalentProvincial() != null) entity.setTalentProvincial(request.getTalentProvincial());
        if (request.getTalentCity() != null) entity.setTalentCity(request.getTalentCity());
        if (request.getTalentRecommend() != null) entity.setTalentRecommend(request.getTalentRecommend());
        if (request.getIpHighValue() != null) entity.setIpHighValue(request.getIpHighValue());
        if (request.getIpClass1Self() != null) entity.setIpClass1Self(request.getIpClass1Self());
        if (request.getIpClass1() != null) entity.setIpClass1(request.getIpClass1());
        if (request.getIpClass2() != null) entity.setIpClass2(request.getIpClass2());
        if (request.getRdStaffRatio() != null) entity.setRdStaffRatio(request.getRdStaffRatio());
        if (request.getRdInstitutionLevel() != null) entity.setRdInstitutionLevel(request.getRdInstitutionLevel());
        if (request.getDirectTechAward() != null) entity.setDirectTechAward(request.getDirectTechAward());
        if (request.getDirectNationalAward() != null) entity.setDirectNationalAward(request.getDirectNationalAward());
        if (request.getDirectChuangke() != null) entity.setDirectChuangke(request.getDirectChuangke());
        if (request.getFormData() != null) entity.setFormData(request.getFormData());
        if (request.getCreditCode() != null) entity.setCreditCode(request.getCreditCode());
        if (request.getLegalRepresentative() != null) entity.setLegalRepresentative(request.getLegalRepresentative());
        if (request.getEstDate() != null) entity.setEstDate(request.getEstDate());
        if (request.getFullAddress() != null) entity.setFullAddress(request.getFullAddress());
        if (request.getRegisteredCapital() != null) entity.setRegisteredCapital(request.getRegisteredCapital());
        if (request.getProvince() != null) entity.setProvince(request.getProvince());
        if (request.getCity() != null) entity.setCity(request.getCity());
        if (request.getDistrict() != null) entity.setDistrict(request.getDistrict());
        if (request.getContactPerson() != null) entity.setContactPerson(request.getContactPerson());
        if (request.getContactPhone() != null) entity.setContactPhone(request.getContactPhone());
        if (request.getContactMobile() != null) entity.setContactMobile(request.getContactMobile());
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
        if (request.getEmployees() != null) entity.setEmployees(request.getEmployees());
        if (request.getRdStaffCount() != null) entity.setRdStaffCount(request.getRdStaffCount());
        return declarationRepo.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        materialRepo.findByDeclarationId(id).forEach(m -> materialRepo.delete(m));
        declarationRepo.deleteById(id);
    }

    // ==================== 审核操作 ====================

    @Transactional
    public Declaration review(Long id, String status, String reviewComment) {
        Declaration entity = get(id);
        entity.setStatus(status);
        entity.setReviewComment(reviewComment);
        entity.setReviewTime(java.time.LocalDateTime.now());
        return declarationRepo.save(entity);
    }

    // ==================== 材料操作 ====================

    /**
     * OCR识别营业执照，自动查找该申报的"营业执照（副本）"材料记录
     */
    @Transactional
    public Map<String, Object> recognizeBusinessLicense(MultipartFile file, Long declarationId) {
        Declaration declaration = get(declarationId);

        List<DeclarationMaterial> materials = materialRepo.findByDeclarationId(declarationId);
        DeclarationMaterial blMaterial = materials.stream()
                .filter(m -> "营业执照（副本）".equals(m.getMaterialName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到营业执照材料记录，请先创建申报"));

        return ocrService.recognize(file, declarationId, blMaterial.getId(), declaration.getEnterpriseId(), blMaterial.getMaterialName());
    }

    /**
     * 通用 OCR：对指定材料进行识别并提取字段（所有材料通用入口）
     */
    @Transactional
    public Map<String, Object> recognizeMaterial(MultipartFile file, Long declarationId, String materialName) {
        Declaration declaration = get(declarationId);

        List<DeclarationMaterial> materials = materialRepo.findByDeclarationId(declarationId);
        DeclarationMaterial material = materials.stream()
                .filter(m -> materialName.equals(m.getMaterialName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到材料记录：" + materialName));

        return ocrService.recognize(file, declarationId, material.getId(), declaration.getEnterpriseId(), material.getMaterialName());
    }

    /**
     * 上传材料文件（仅保存文件到 document，更新 material 状态，不触发 OCR）
     */
    @Transactional
    public DeclarationMaterial uploadMaterialFile(Long declarationId, Long materialId, MultipartFile file) {
        Declaration declaration = get(declarationId);
        DeclarationMaterial material = materialRepo.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("材料不存在"));

        String category = material.getMaterialType();
        Document doc = fileStorageService.store(file, declaration.getEnterpriseId(), declarationId, category);

        material.setDocumentId(doc.getId());
        material.setStatus("UPLOADED");
        return materialRepo.save(material);
    }

    public List<DeclarationMaterial> listMaterials(Long declarationId) {
        return materialRepo.findByDeclarationId(declarationId);
    }

    @Transactional
    public DeclarationMaterial addMaterial(Long declarationId, DeclarationMaterial material) {
        material.setDeclarationId(declarationId);
        return materialRepo.save(material);
    }

    @Transactional
    public DeclarationMaterial updateMaterial(Long materialId, DeclarationMaterial request) {
        DeclarationMaterial entity = materialRepo.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("材料不存在"));
        if (request.getMaterialType() != null) entity.setMaterialType(request.getMaterialType());
        if (request.getMaterialName() != null) entity.setMaterialName(request.getMaterialName());
        if (request.getRequired() != null) entity.setRequired(request.getRequired());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getDocumentId() != null) entity.setDocumentId(request.getDocumentId());
        if (request.getIdentifyResult() != null) entity.setIdentifyResult(request.getIdentifyResult());
        return materialRepo.save(entity);
    }

    @Transactional
    public void deleteMaterial(Long materialId) {
        materialRepo.deleteById(materialId);
    }

    // ==================== 统计 ====================

    public Map<String, Object> getStats() {
        List<Declaration> all = declarationRepo.findAll();
        long total = all.size();
        long pending = all.stream().filter(d -> "PENDING".equals(d.getStatus()) || "SUBMITTED".equals(d.getStatus())).count();
        long approved = all.stream().filter(d -> "APPROVED".equals(d.getStatus())).count();
        long rejected = all.stream().filter(d -> "REJECTED".equals(d.getStatus())).count();
        return Map.of("total", total, "pending", pending, "approved", approved, "rejected", rejected, "draft",
                all.stream().filter(d -> "DRAFT".equals(d.getStatus())).count());
    }

    // ==================== 辅助方法 ====================

    private Page<Declaration> toPage(List<Declaration> items, int page, int size) {
        int fromIndex = Math.min(page * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        return new PageImpl<>(items.subList(fromIndex, toIndex), PageRequest.of(page, size), items.size());
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword.toLowerCase());
    }
}
