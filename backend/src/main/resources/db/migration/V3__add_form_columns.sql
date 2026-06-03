-- Enterprise Application — V3: 补充 Declaration 新增字段
-- 配合 JPA ddl-auto=update，确保列存在

ALTER TABLE declaration
    ADD COLUMN IF NOT EXISTS form_data MEDIUMTEXT COMMENT '完整表单JSON',
    ADD COLUMN IF NOT EXISTS credit_code VARCHAR(64) COMMENT '统一社会信用代码',
    ADD COLUMN IF NOT EXISTS legal_representative VARCHAR(64) COMMENT '法定代表人',
    ADD COLUMN IF NOT EXISTS est_date VARCHAR(32) COMMENT '注册时间',
    ADD COLUMN IF NOT EXISTS full_address VARCHAR(255) COMMENT '通讯地址',
    ADD COLUMN IF NOT EXISTS registered_capital DOUBLE COMMENT '注册资本（万元）',
    ADD COLUMN IF NOT EXISTS province VARCHAR(32),
    ADD COLUMN IF NOT EXISTS city VARCHAR(32),
    ADD COLUMN IF NOT EXISTS district VARCHAR(32),
    ADD COLUMN IF NOT EXISTS contact_person VARCHAR(64) COMMENT '联系人',
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(32) COMMENT '电话',
    ADD COLUMN IF NOT EXISTS contact_mobile VARCHAR(32) COMMENT '手机',
    ADD COLUMN IF NOT EXISTS email VARCHAR(128),
    ADD COLUMN IF NOT EXISTS employees INT COMMENT '全职员工数量',
    ADD COLUMN IF NOT EXISTS rd_staff_count INT COMMENT '研发人员数量';
