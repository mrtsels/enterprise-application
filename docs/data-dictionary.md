---
name: data-dictionary
description: "专精特新\"小巨人\"申报系统数据库字段字典，涵盖企业基本信息、财务指标、创新能力等12个模块"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 3e1bafa7-920b-451b-acba-341ad47ec06c
---

# 数据字典 — 专精特新"小巨人"申报系统

> 适用于第八批专精特新"小巨人"申报系统、Excel导入模板、动态表单系统设计。
>
> 主表字段约 **120** 个，子表字段约 **30~50** 个，总字段数约 **150~180** 个。

---

## 一、企业基本信息

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 企业名称 | `company_name` | String |
| 企业注册地-省 | `registered_province` | String |
| 企业注册地-市（区） | `registered_city` | String |
| 企业注册地-县 | `registered_county` | String |
| 通讯地址 | `address` | String |
| 邮编 | `postal_code` | String |
| 法定代表人 | `legal_representative` | String |
| 控股股东 | `controlling_shareholder` | String |
| 实际控制人 | `actual_controller` | String |
| 实际控制人国籍 | `controller_nationality` | String |
| 联系人 | `contact_person` | String |
| 联系电话 | `contact_phone` | String |
| 联系手机 | `contact_mobile` | String |
| 传真 | `fax` | String |
| 邮箱 | `email` | Email |
| 注册时间 | `registration_date` | Date |
| 注册资本（万元） | `registered_capital` | Decimal |
| 统一社会信用代码 | `unified_social_credit_code` | String |
| 企业规模 | `enterprise_scale` | Enum |
| 所属行业代码 | `industry_code` | String |
| 所属行业名称 | `industry_name` | String |
| 细分领域代码 | `sub_industry_code` | String |
| 细分领域名称 | `sub_industry_name` | String |
| 企业类型 | `enterprise_type` | Enum |
| 是否存在小巨人控股关系 | `has_little_giant_relation` | Boolean |
| 关联小巨人企业名称 | `related_little_giant_company` | String |
| 是否存在同集团相似产品企业 | `has_group_similar_company` | Boolean |
| 同集团企业名称 | `group_company_name` | String |

---

## 二、上市情况

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 上市状态 | `listing_status` | Enum |
| 股票代码 | `stock_code` | String |
| 上市进程 | `listing_progress` | Enum |
| 拟上市地点 | `planned_listing_exchange` | Enum |

---

## 三、经济效益和经营情况（2023-2025）

### 人员指标

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 2023全职员工数量 | `employee_count_2023` | Integer |
| 2024全职员工数量 | `employee_count_2024` | Integer |
| 2025全职员工数量 | `employee_count_2025` | Integer |
| 2023研发人员数量 | `rd_employee_count_2023` | Integer |
| 2024研发人员数量 | `rd_employee_count_2024` | Integer |
| 2025研发人员数量 | `rd_employee_count_2025` | Integer |

### 财务指标

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 2023营业收入 | `revenue_2023` | Decimal |
| 2024营业收入 | `revenue_2024` | Decimal |
| 2025营业收入 | `revenue_2025` | Decimal |
| 2023主营业务收入 | `main_revenue_2023` | Decimal |
| 2024主营业务收入 | `main_revenue_2024` | Decimal |
| 2025主营业务收入 | `main_revenue_2025` | Decimal |
| 2023主营业务收入增长率 | `main_revenue_growth_2023` | Percentage |
| 2024主营业务收入增长率 | `main_revenue_growth_2024` | Percentage |
| 2025主营业务收入增长率 | `main_revenue_growth_2025` | Percentage |
| 2023利润总额 | `total_profit_2023` | Decimal |
| 2024利润总额 | `total_profit_2024` | Decimal |
| 2025利润总额 | `total_profit_2025` | Decimal |
| 2023净利润 | `net_profit_2023` | Decimal |
| 2024净利润 | `net_profit_2024` | Decimal |
| 2025净利润 | `net_profit_2025` | Decimal |
| 2023净利润增长率 | `net_profit_growth_2023` | Percentage |
| 2024净利润增长率 | `net_profit_growth_2024` | Percentage |
| 2025净利润增长率 | `net_profit_growth_2025` | Percentage |
| 2023销售费用 | `selling_expense_2023` | Decimal |
| 2024销售费用 | `selling_expense_2024` | Decimal |
| 2025销售费用 | `selling_expense_2025` | Decimal |
| 2023管理费用 | `admin_expense_2023` | Decimal |
| 2024管理费用 | `admin_expense_2024` | Decimal |
| 2025管理费用 | `admin_expense_2025` | Decimal |
| 2023营业成本 | `operating_cost_2023` | Decimal |
| 2024营业成本 | `operating_cost_2024` | Decimal |
| 2025营业成本 | `operating_cost_2025` | Decimal |
| 2023主营业务成本 | `main_cost_2023` | Decimal |
| 2024主营业务成本 | `main_cost_2024` | Decimal |
| 2025主营业务成本 | `main_cost_2025` | Decimal |
| 2023产品销售成本 | `product_cost_2023` | Decimal |
| 2024产品销售成本 | `product_cost_2024` | Decimal |
| 2025产品销售成本 | `product_cost_2025` | Decimal |
| 2023资产总额 | `total_assets_2023` | Decimal |
| 2024资产总额 | `total_assets_2024` | Decimal |
| 2025资产总额 | `total_assets_2025` | Decimal |
| 2023期末净资产 | `net_assets_2023` | Decimal |
| 2024期末净资产 | `net_assets_2024` | Decimal |
| 2025期末净资产 | `net_assets_2025` | Decimal |
| 2023负债总额 | `liabilities_2023` | Decimal |
| 2024负债总额 | `liabilities_2024` | Decimal |
| 2025负债总额 | `liabilities_2025` | Decimal |
| 2023资产负债率 | `debt_ratio_2023` | Percentage |
| 2024资产负债率 | `debt_ratio_2024` | Percentage |
| 2025资产负债率 | `debt_ratio_2025` | Percentage |
| 2023上缴税金 | `tax_paid_2023` | Decimal |
| 2024上缴税金 | `tax_paid_2024` | Decimal |
| 2025上缴税金 | `tax_paid_2025` | Decimal |
| 2023股权融资总额 | `equity_financing_2023` | Decimal |
| 2024股权融资总额 | `equity_financing_2024` | Decimal |
| 2025股权融资总额 | `equity_financing_2025` | Decimal |
| 2023对应估值 | `valuation_2023` | Decimal |
| 2024对应估值 | `valuation_2024` | Decimal |
| 2025对应估值 | `valuation_2025` | Decimal |
| 2023银行贷款 | `bank_loan_2023` | Decimal |
| 2024银行贷款 | `bank_loan_2024` | Decimal |
| 2025银行贷款 | `bank_loan_2025` | Decimal |
| 2023境内债券 | `domestic_bond_2023` | Decimal |
| 2024境内债券 | `domestic_bond_2024` | Decimal |
| 2025境内债券 | `domestic_bond_2025` | Decimal |
| 2023境外债券 | `overseas_bond_2023` | Decimal |
| 2024境外债券 | `overseas_bond_2024` | Decimal |
| 2025境外债券 | `overseas_bond_2025` | Decimal |

---

## 四、融资情况

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 审计报告编码 | `audit_report_code` | String |
| 是否申请银行贷款 | `applied_bank_loan` | Boolean |
| 信贷满足率 | `credit_satisfaction_rate` | Percentage |
| 贷款用途 | `loan_usage` | MultiSelect |
| 资金需求额 | `funding_requirement` | Decimal |
| 融资方式 | `financing_method` | MultiSelect |

---

## 五、专业化

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 从事细分市场时间 | `niche_market_years` | Integer |
| 主营业务收入占比 | `main_business_ratio` | Percentage |
| 近2年主营业务平均增长率 | `avg_main_business_growth` | Percentage |

### 产品表（products）

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 产品名称 | `product_name` | String |
| 产品收入 | `product_revenue` | Decimal |
| 日产能 | `daily_capacity` | Decimal |
| 产能单位 | `capacity_unit` | String |
| 知识产权名称 | `ip_name` | String |
| 知识产权附件 | `ip_attachment` | File |

---

## 六、精细化

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 管理体系认证 | `management_certifications` | MultiSelect |
| 信息系统支撑 | `information_systems` | MultiSelect |
| 国际认证 | `international_certifications` | MultiSelect |
| 国内认证 | `domestic_certifications` | MultiSelect |
| 业务系统云迁移 | `cloud_migration` | Boolean |
| 制造业互联网示范项目 | `internet_demo_project` | Boolean |

---

## 七、特色化

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 2024国际市场占有率 | `international_share_2024` | Percentage |
| 2025国际市场占有率 | `international_share_2025` | Percentage |
| 2024国内市场占有率 | `domestic_share_2024` | Percentage |
| 2025国内市场占有率 | `domestic_share_2025` | Percentage |
| 市场占有率说明 | `market_share_description` | LongText |
| 2024出口额 | `export_amount_2024` | Decimal |
| 2025出口额 | `export_amount_2025` | Decimal |
| 出口目的地 | `export_destinations` | JSON Array |
| 2024品牌数量 | `brand_count_2024` | Integer |
| 2025品牌数量 | `brand_count_2025` | Integer |
| 2024品牌销售收入 | `brand_revenue_2024` | Decimal |
| 2025品牌销售收入 | `brand_revenue_2025` | Decimal |

---

## 八、创新能力

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 研发机构建设情况 | `rd_institutions` | JSON |
| 院士工作站 | `academician_station` | Boolean |
| 博士后工作站 | `postdoctoral_station` | Boolean |
| 合作机构 | `partner_organizations` | JSON Array |
| 成果应用情况 | `achievement_description` | LongText |
| 2023研发费用 | `rd_expense_2023` | Decimal |
| 2024研发费用 | `rd_expense_2024` | Decimal |
| 2025研发费用 | `rd_expense_2025` | Decimal |
| 2023研发费用占比 | `rd_ratio_2023` | Percentage |
| 2024研发费用占比 | `rd_ratio_2024` | Percentage |
| 2025研发费用占比 | `rd_ratio_2025` | Percentage |
| 2023研发人员占比 | `rd_employee_ratio_2023` | Percentage |
| 2024研发人员占比 | `rd_employee_ratio_2024` | Percentage |
| 2025研发人员占比 | `rd_employee_ratio_2025` | Percentage |
| I类知识产权总数 | `class1_ip_count` | Integer |
| 发明专利数 | `invention_patent_count` | Integer |

---

## 九、科技奖励

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 国家级科技奖励 | `national_award` | JSON |
| 省级科技奖励 | `provincial_award` | JSON |
| 创客中国50强 | `maker_china_top50` | JSON |

---

## 十、产业链配套

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 所属产业链 | `industry_chain` | String |
| 是否补短板填空白 | `fill_gap_flag` | Boolean |
| 补短板产品 | `fill_gap_product` | String |
| 填补空白领域 | `fill_gap_field` | String |
| 替代进口产品 | `import_substitution_product` | String |
| 补短板说明 | `fill_gap_description` | LongText |
| 是否直接配套知名企业 | `direct_supply_flag` | Boolean |
| 配套企业列表 | `direct_supply_companies` | JSON Array |
| 行业领军企业列表 | `leading_companies` | JSON Array |

---

## 十一、主导产品所属领域

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 主导产品名称 | `leading_product_name` | String |
| 主导产品类别 | `leading_product_category` | String |
| 是否属于工业六基 | `industrial_six_basics` | Boolean |
| 六基分类 | `six_basics_type` | Enum |

---

## 十二、其他

| 显示名称 | 数据库字段名 | 类型 |
|---------|-------------|------|
| 标准制定情况 | `standards_info` | JSON |
| 企业荣誉资质 | `honors` | JSON |
| 境外经营情况 | `overseas_operations` | JSON |
| 国家重大科技项目 | `national_science_projects` | JSON |
| 国家技术创新项目 | `national_innovation_projects` | JSON |
| 企业总体情况介绍 | `company_profile` | LongText |

---

## 推荐数据库表结构

```text
enterprise_application
├── basic_info              # 企业基本信息（一）
├── financial_metrics       # 经济效益与经营（三）
├── financing_info          # 融资情况（四）
├── products                # 产品表（五）
├── certifications          # 精细化认证（六）
├── innovation_info         # 创新能力（八）
├── awards                  # 科技奖励（九）
├── industry_chain          # 产业链配套（十）
├── standards               # 标准制定
├── honors                  # 企业荣誉资质
├── overseas_operations     # 境外经营情况
└── attachments             # 附件
```
