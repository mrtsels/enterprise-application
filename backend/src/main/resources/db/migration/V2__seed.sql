-- 种子数据：测试企业和申报数据

INSERT IGNORE INTO enterprise (id, username, password_hash, name, credit_code, area, industry, scale, contact_name, contact_phone, annual_revenue, rd_investment, rd_ratio, debt_ratio, net_profit, employees, patent_count, market_share, status, inventory_status, created_at, updated_at)
VALUES
(1, 'test_enterprise', 'dummy_hash', '广州测试科技有限公司', '91440101MA5XXXXXXX', '海珠区', '软件和信息技术服务业', '中型', '张三', '13800138000', 5000, 400, 8.0, 35.0, 800, 120, 15, 2.5, 'NORMAL', '在库', NOW(), NOW());

INSERT IGNORE INTO declaration (id, enterprise_id, enterprise_name, type, level, category, year, status, submit_time, revenue, rd_investment, rd_ratio, debt_ratio, patent_count, product_name, material_completeness, total_score, professional_score, refined_score, feature_score, innovation_score_detail, declaration_eligible, created_at, updated_at)
VALUES
(1, 1, '广州测试科技有限公司', 'XIAOJUREN', '国家级', '认定', 2026, 'DRAFT', NULL, 5000, 400, 8.0, 35.0, 15, '智能测试平台', 0, 0, 0, 0, 0, 0, NULL, NOW(), NOW());
