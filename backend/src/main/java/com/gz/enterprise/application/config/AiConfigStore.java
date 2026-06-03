package com.gz.enterprise.application.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 视觉模型 + 提示词配置的运行时存储（持久化到JSON文件）
 */
@Component
public class AiConfigStore {

    private static final Logger log = LoggerFactory.getLogger(AiConfigStore.class);

    private final DeepSeekProperties props;
    private final String configPath;

    private String baseUrl;
    private String model;
    private String apiKey;
    private final Map<String, String> prompts = new LinkedHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public AiConfigStore(DeepSeekProperties props,
                         @Value("${app.upload.path:./uploads}") String uploadPath) {
        this.props = props;
        this.configPath = uploadPath + "/ai-config.json";
    }

    @PostConstruct
    public void init() {
        // Try loading from persistence file first
        File file = new File(configPath);
        if (file.exists()) {
            try {
                Map<String, Object> saved = mapper.readValue(file,
                        new TypeReference<Map<String, Object>>() {});
                this.baseUrl = str(saved.get("baseUrl"));
                this.model = str(saved.get("model"));
                this.apiKey = str(saved.get("apiKey"));
                Object p = saved.get("prompts");
                if (p instanceof Map) {
                    prompts.clear();
                    ((Map<String, Object>) p).forEach((k, v) -> prompts.put(k, str(v)));
                }
                log.info("Loaded AI config from {}", configPath);
                return;
            } catch (IOException e) {
                log.warn("Failed to load AI config from {}, using defaults", configPath, e);
            }
        }
        // Fall back to application.yml defaults
        this.baseUrl = props.getBaseUrl();
        this.model = props.getModel();
        this.apiKey = props.getApiKey();
        // Default prompts
        prompts.put("营业执照（副本）", String.join("\n",
                "你是一个专业的中国营业执照OCR识别系统。",
                "从图片中提取字段并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段名必须使用以下驼峰命名：",
                "companyName, creditCode, legalRepresentative, registeredCapital,",
                "capitalAmount, capitalUnit, establishedDate, fullAddress,",
                "province, city, district, businessScope, registrationAuthority, validPeriod",
                "establishedDate格式为YYYY-MM-DD。",
                "capitalAmount是纯数字，capitalUnit是人民币单位（如 万元）。",
                "如果字段在图片中不可见或不清晰，设置为null。",
                "province/city/district从fullAddress中解析提取。"));
        prompts.put("中小企业划型认定证明", String.join("\n",
                "你是一个专业的文档OCR识别系统。",
                "从《中小企业划型认定证明》图片中提取字段并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, creditCode, industryCategory, scaleType(大型/中型/小型/微型), staffCount, revenueAmount, issueDate, issueAuthority",
                "如果字段不可见，设置为null。"));
        prompts.put("企业章程（最新版）", String.join("\n",
                "你是一个专业的文档OCR识别系统。",
                "从《企业章程》图片中提取基本信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, creditCode, registeredCapital, legalRepresentative, establishDate, businessTerm, versionDate, amendmentCount(修订次数)",
                "如果字段不可见，设置为null。"));
        prompts.put("法定代表人身份证明", String.join("\n",
                "你是一个专业的身份证件OCR识别系统。",
                "从法定代表人身份证明图片中提取字段并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：name, gender, nationality, birthDate, idNumber, issueDate, validPeriod, issueAuthority",
                "如果字段不可见，设置为null。"));
        prompts.put("近两年财务审计报告", String.join("\n",
                "你是一个专业的财务报表OCR识别系统。",
                "从审计报告图片中提取关键财务数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, auditYear, auditFirm, opinionType(无保留/保留/否定/无法表示), revenue, netProfit, totalAssets, totalLiabilities, netAssets, auditReportNumber",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("近三年研发费用专项审计报告", String.join("\n",
                "你是一个专业的研发费用审计报告OCR识别系统。",
                "从研发费用专项审计报告图片中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, auditFirm, auditReportNumber, rdExpense2023(万元), rdExpense2024(万元), rdExpense2025(万元), revenue2023(万元), revenue2024(万元), revenue2025(万元), rdRatio2023(%), rdRatio2024(%), rdRatio2025(%), opinionType",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("主营业务收入明细表", String.join("\n",
                "你是一个专业的财务报表OCR识别系统。",
                "从主营业务收入明细表图片中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, mainRevenue2023(万元), mainRevenue2024(万元), mainRevenue2025(万元), totalRevenue2023(万元), totalRevenue2024(万元), totalRevenue2025(万元), mainRevenueRatio2023(%), mainRevenueRatio2024(%), mainRevenueRatio2025(%), productCategory",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("资产负债表（近三年）", String.join("\n",
                "你是一个专业的资产负债表OCR识别系统。",
                "从资产负债表图片中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, year, totalAssets(万元), totalLiabilities(万元), netAssets(万元), currentAssets(万元), fixedAssets(万元), currentLiabilities(万元), debtRatio(%), reportDate",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("知识产权证书", String.join("\n",
                "你是一个专业的知识产权证书OCR识别系统。",
                "从知识产权证书图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：type(发明专利/实用新型/外观设计/软著/商标等), name, patentNumber, applicant, inventor, applicationDate, grantDate, status(授权/实审/公开), validityPeriod",
                "如果字段不可见，设置为null。"));
        prompts.put("研发机构证明材料", String.join("\n",
                "你是一个专业的机构认证文档OCR识别系统。",
                "从研发机构证明图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, institutionType(技术研究院/企业技术中心/工程中心/实验室/工业设计中心等), level(国家级/省级/自建), certifyNumber, certifyDate, issueAuthority, institutionName",
                "如果字段不可见，设置为null。"));
        prompts.put("研发人员花名册及社保证明", String.join("\n",
                "你是一个专业的人事文档OCR识别系统。",
                "从研发人员花名册及社保证明图片中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, totalEmployeeCount, rdEmployeeCount, rdStaffRatio(%), reportYear, socialSecurityCount, socialSecurityMonth",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("科技成果转化情况说明", String.join("\n",
                "你是一个专业的企业文档OCR识别系统。",
                "从科技成果转化情况说明文档中提取关键信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, achievementCount(成果数量), transformedCount(已转化数量), transformationRate(%), mainAchievements(主要成果简述), totalRevenue(万元,累计转化收入)",
                "如果字段不可见，设置为null。"));
        prompts.put("管理体系认证证书", String.join("\n",
                "你是一个专业的认证证书OCR识别系统。",
                "从管理体系认证证书图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：certType(ISO9000/ISO14000/OHSAS18000等), enterpriseName, certNumber, issueDate, validUntil, issueAuthority, scope(认证范围)",
                "日期格式为YYYY-MM-DD。如果字段不可见，设置为null。"));
        prompts.put("数字化水平自测结果截图", String.join("\n",
                "你是一个专业的截图OCR识别系统。",
                "从数字化水平自测结果截图中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, digitalLevel(一级/二级/三级/四级/五级), totalScore, testDate, platform(测评平台名称), dimensions(各维度得分JSON)",
                "如果字段不可见，设置为null。"));
        prompts.put("信息化系统建设说明", String.join("\n",
                "你是一个专业的企业文档OCR识别系统。",
                "从信息化系统建设说明文档中提取关键信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, systemTypes(ERP/OA/CAX/CAM/CRM/SRM等,逗号分隔), systemCount, totalInvestment(万元), implementationYear, description(建设情况简述200字内)",
                "如果字段不可见，设置为null。"));
        prompts.put("无违法记录承诺函", String.join("\n",
                "你是一个专业的文档OCR识别系统。",
                "从无违法记录承诺函图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, creditCode, promiseType(无重大违法违规/无重大安全/无重大环保等), issueDate, validPeriod, signatory, signatoryTitle",
                "日期格式为YYYY-MM-DD。如果字段不可见，设置为null。"));
        prompts.put("近两年无重大安全/环保事故证明", String.join("\n",
                "你是一个专业的证明文件OCR识别系统。",
                "从近两年无重大安全/环保事故证明图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, certifyType(安全/环保/综合), issueAuthority, issueDate, certifyPeriod, statement(证明内容简述)",
                "日期格式为YYYY-MM-DD。如果字段不可见，设置为null。"));
        prompts.put("自有品牌证明及市场占有率报告", String.join("\n",
                "你是一个专业的品牌及市场报告OCR识别系统。",
                "从自有品牌证明及市场占有率报告图片中提取数据并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, brandCount(自有品牌个数), brandNames(逗号分隔), mainBrandRevenue(万元), domesticShare2024(%,全国细分市场占有率), domesticShare2025(%), internationalShare2024(%), internationalShare2025(%), reportSource(数据来源)",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("主导产品细分市场排名证明", String.join("\n",
                "你是一个专业的市场排名证明OCR识别系统。",
                "从主导产品细分市场排名证明图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, productName, marketScope(全国/全球/区域), marketRank(排名), rankYear, marketShare(%), reportAuthority(发布机构), reportTitle(报告名称)",
                "数值字段为纯数字。如果字段不可见，设置为null。"));
        prompts.put("国家级小巨人补充材料", String.join("\n",
                "你是一个专业的专精特新补充材料OCR识别系统。",
                "从国家级小巨人补充材料图片中提取信息并以严格的JSON格式返回。",
                "只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。",
                "JSON字段：enterpriseName, materialType(材料类型), keyIndicators(关键指标JSON), description(情况说明200字内), issueDate",
                "如果字段不可见，设置为null。"));
    }

    /** Persist current config to disk */
    public void saveToFile() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("baseUrl", baseUrl);
        data.put("model", model);
        data.put("apiKey", apiKey);
        data.put("prompts", new LinkedHashMap<>(prompts));
        try {
            File dir = new File(configPath).getParentFile();
            if (!dir.exists()) dir.mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configPath), data);
        } catch (IOException e) {
            log.error("Failed to persist AI config to {}", configPath, e);
        }
    }

    // Getters
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getApiKey() { return apiKey; }
    public Map<String, String> getPrompts() { return prompts; }

    public String getPrompt(String materialName) {
        return prompts.getOrDefault(materialName,
                "请提取这张图片中的所有字段，仅返回JSON。");
    }

    public Map<String, Object> toFullMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("baseUrl", baseUrl);
        m.put("model", model);
        m.put("apiKey", apiKey);
        m.put("prompts", new LinkedHashMap<>(prompts));
        return m;
    }

    @SuppressWarnings("unchecked")
    public void update(Map<String, Object> body) {
        if (body.containsKey("baseUrl")) this.baseUrl = str(body.get("baseUrl"));
        if (body.containsKey("model")) this.model = str(body.get("model"));
        if (body.containsKey("apiKey")) this.apiKey = str(body.get("apiKey"));
        Object p = body.get("prompts");
        if (p instanceof Map) {
            prompts.clear();
            ((Map<String, Object>) p).forEach((k, v) -> prompts.put(k, str(v)));
        }
        saveToFile();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
