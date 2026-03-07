CREATE TABLE drugs
(
    drug_id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '药品ID',
    drug_name         VARCHAR(255) NOT NULL COMMENT '药品名称',
    specification     VARCHAR(255) NOT NULL COMMENT '药品规格',
    manufacturer      VARCHAR(255) NOT NULL COMMENT '生产厂家',
    approval_number   VARCHAR(255) NOT NULL COMMENT '批准文号',
    expiry_date       DATE         NOT NULL COMMENT '有效期',
    category          VARCHAR(255) NOT NULL COMMENT '药品分类',
    dosage_form       VARCHAR(255) NOT NULL COMMENT '药品剂型',
    ingredients       TEXT         NOT NULL COMMENT '药品成分',
    indications       TEXT         NOT NULL COMMENT '适应症',
    dosage            TEXT         NOT NULL COMMENT '用法用量',
    side_effects      TEXT         NOT NULL COMMENT '不良反应',
    contraindications TEXT         NOT NULL COMMENT '禁忌',
    precautions       TEXT         NOT NULL COMMENT '注意事项',
    interactions      TEXT         NOT NULL COMMENT '药物相互作用',
    image_url         VARCHAR(255) COMMENT '药品图片URL'
) COMMENT ='药品表';

CREATE TABLE prescription_details
(
    detail_id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    prescription_id    INT          NOT NULL COMMENT '处方ID',
    drug_id            INT          NOT NULL COMMENT '药品ID',
    quantity           INT          NOT NULL COMMENT '药品数量',
    usage_instructions VARCHAR(255) NOT NULL COMMENT '用法',
    dosage             VARCHAR(255) NOT NULL COMMENT '用量'
) COMMENT ='处方明细表';

create table patient_outpatient
(
    id               bigint auto_increment comment '门诊服务ID' primary key,
    doctor_id        bigint                                                           not null comment '医生ID',
    patient_id       bigint                                                           not null comment '患者ID',
    allergy_history  text                                                             null comment '过敏史',
    medical_history  text                                                             null comment '患病史',
    family_history   text                                                             null comment '家族史',
    personal_history text                                                             null comment '个人史',
    comment          text                                                             null comment '备注',
    creator          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '创建者',
    create_time      datetime                               default CURRENT_TIMESTAMP not null comment '创建时间',
    updater          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '更新者',
    update_time      datetime                               default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          bit                                    default b'0'              not null comment '是否删除',
    tenant_id        bigint                                 default 0                 not null comment '租户编号'
) comment '门诊药学服务表';

create table patient_outpatient_check
(
    id               bigint auto_increment comment '审核ID' primary key,
    outpatient_id    bigint                                                           not null comment '门诊药学ID',
    medicine_name    bigint                                                           not null comment '不合理用药名称',
    doctor_id        bigint                                                           null comment '处方医生',
    prescription_id  bigint                                                           null comment '处方ID',
    issue_desc       text                                                             null comment '问题描述',
    issue_code       varchar(255)                                                     null comment '问题代码',
    intervene_result text                                                             null comment '干预结果',
    creator          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '创建者',
    create_time      datetime                               default CURRENT_TIMESTAMP not null comment '创建时间',
    updater          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '更新者',
    update_time      datetime                               default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          bit                                    default b'0'              not null comment '是否删除',
    tenant_id        bigint                                 default 0                 not null comment '租户编号'
) comment '处方审核';

create table patient_outpatient_medicine
(
    id               bigint auto_increment comment '药学门诊' primary key,
    medicine_name    bigint                                                           not null comment '药品名称',
    dosage        varchar(255)                                                           null comment '用法用量',
    start_time  datetime                                                           null comment '开始使用时间',
    stop_time       datetime                                                             null comment '停止使用时间',
    stop_reason       varchar(255)                                                     null comment '停药原因',
    mrp_type varchar(255)                                                             null comment 'MRP类别',
    weight varchar(255)                                                             null comment '权重',
    mrp_desc varchar(255)                                                             null comment 'MRP描述',
    creator          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '创建者',
    create_time      datetime                               default CURRENT_TIMESTAMP not null comment '创建时间',
    updater          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '更新者',
    update_time      datetime                               default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          bit                                    default b'0'              not null comment '是否删除',
    tenant_id        bigint                                 default 0                 not null comment '租户编号'
) comment '药学门诊';

create table patient_eduction
(
    id               bigint auto_increment comment '咨询ID' primary key,
    medicine_name    bigint                                                           not null comment '药品名称',
    patient_id        bigint                                                           null comment '患者ID',
    disease  varchar(255)                                                           null comment '疾病',
    content       varchar(255)                                                             null comment '教育内容',
    content_basis       varchar(255)                                                     null comment '教育依据',
    creator          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '创建者',
    create_time      datetime                               default CURRENT_TIMESTAMP not null comment '创建时间',
    updater          varchar(64) collate utf8mb4_unicode_ci default ''                null comment '更新者',
    update_time      datetime                               default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          bit                                    default b'0'              not null comment '是否删除',
    tenant_id        bigint                                 default 0                 not null comment '租户编号'
) comment '用药教育';

