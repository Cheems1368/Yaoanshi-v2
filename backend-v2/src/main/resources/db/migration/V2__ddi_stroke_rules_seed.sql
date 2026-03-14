CREATE TABLE IF NOT EXISTS ddi_rule (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL,
    drug_a_name VARCHAR(128) NOT NULL,
    drug_b_name VARCHAR(128) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    message VARCHAR(255) NOT NULL,
    recommended_action VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    maintained_by_pharmacist_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_ddi_rule_code (rule_code),
    KEY idx_ddi_severity (tenant_id, severity)
);

CREATE TABLE IF NOT EXISTS drug_contraindication (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    condition_type VARCHAR(32) NOT NULL,
    condition_value VARCHAR(128) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    message VARCHAR(255) NOT NULL,
    recommended_action VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    maintained_by_pharmacist_id BIGINT NULL,
    creator VARCHAR(64) NULL,
    create_time DATETIME NOT NULL,
    updater VARCHAR(64) NULL,
    update_time DATETIME NOT NULL,
    deleted BIT(1) NOT NULL DEFAULT b'0',
    tenant_id BIGINT NOT NULL,
    UNIQUE KEY uk_contraindication_rule_code (rule_code),
    KEY idx_contraindication_drug (tenant_id, drug_name),
    KEY idx_contraindication_condition (tenant_id, condition_type, condition_value),
    KEY idx_contraindication_severity (tenant_id, severity)
);

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-ASPIRIN-WARFARIN-CN', '阿司匹林', '华法林', 'HIGH',
       '阿司匹林与华法林联用会明显增加出血风险。',
       '继续使用前需由药师或医生复核抗栓方案，并重点监测出血表现。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-WARFARIN-CN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-CLOPIDOGREL-WARFARIN', '氯吡格雷', '华法林', 'HIGH',
       '氯吡格雷与华法林联用会增加严重出血风险。',
       '建议尽快咨询药师，评估是否需要调整抗凝或抗血小板方案。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-CLOPIDOGREL-WARFARIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-ASPIRIN-CLOPIDOGREL', '阿司匹林', '氯吡格雷', 'HIGH',
       '阿司匹林与氯吡格雷联用会增加出血风险，需严格监测。',
       '如非医生明确要求，不建议患者自行叠加使用双联抗血小板方案。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-CLOPIDOGREL');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-ASPIRIN-RIVAROXABAN', '阿司匹林', '利伐沙班', 'HIGH',
       '阿司匹林与利伐沙班联用会显著增加出血风险。',
       '建议提交前先咨询药师，确认是否存在医生指定的联合抗栓适应证。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-RIVAROXABAN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-ASPIRIN-DABIGATRAN', '阿司匹林', '达比加群', 'HIGH',
       '阿司匹林与达比加群联用会增加严重出血和消化道出血风险。',
       '建议由专业人员确认后再继续，并关注黑便、牙龈出血等异常。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-DABIGATRAN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-CLOPIDOGREL-RIVAROXABAN', '氯吡格雷', '利伐沙班', 'HIGH',
       '氯吡格雷与利伐沙班联用会增加严重出血风险。',
       '建议立即咨询药师或医生，避免自行联合使用。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-CLOPIDOGREL-RIVAROXABAN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-CLOPIDOGREL-DABIGATRAN', '氯吡格雷', '达比加群', 'HIGH',
       '氯吡格雷与达比加群联用会增加严重出血风险。',
       '建议由医生或药师复核治疗必要性，并重点监测出血症状。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-CLOPIDOGREL-DABIGATRAN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-TICAGRELOR-WARFARIN', '替格瑞洛', '华法林', 'HIGH',
       '替格瑞洛与华法林联用会增加严重出血风险。',
       '建议先咨询药师，确认抗栓联合方案是否为医嘱要求。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-TICAGRELOR-WARFARIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-TICAGRELOR-ASPIRIN', '替格瑞洛', '阿司匹林', 'HIGH',
       '替格瑞洛与阿司匹林联用会增加出血风险，大剂量阿司匹林还可能降低替格瑞洛疗效。',
       '如需继续提交，请保留剂量信息并尽快向药师确认。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-TICAGRELOR-ASPIRIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-WARFARIN-IBUPROFEN', '华法林', '布洛芬', 'HIGH',
       '华法林与布洛芬联用会增加出血和胃肠道损伤风险。',
       '建议优先咨询药师，必要时考虑更安全的止痛替代方案。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-WARFARIN-IBUPROFEN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-WARFARIN-DICLOFENAC', '华法林', '双氯芬酸', 'HIGH',
       '华法林与双氯芬酸联用会增加严重出血风险。',
       '建议避免自行合用，并尽快咨询医生或药师。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-WARFARIN-DICLOFENAC');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-ASPIRIN-IBUPROFEN', '阿司匹林', '布洛芬', 'HIGH',
       '阿司匹林与布洛芬联用会增加出血风险，并可能削弱阿司匹林抗血小板作用。',
       '如需止痛，请先咨询药师是否更换药物或调整服药顺序。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-ASPIRIN-IBUPROFEN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-WARFARIN-ATORVASTATIN', '华法林', '阿托伐他汀', 'HIGH',
       '华法林与阿托伐他汀联用可能增强抗凝作用，增加出血风险。',
       '建议继续前监测 INR 或由药师评估是否需要调整剂量。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-WARFARIN-ATORVASTATIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-WARFARIN-METRONIDAZOLE', '华法林', '甲硝唑', 'HIGH',
       '华法林与甲硝唑联用会显著增强抗凝作用并增加出血风险。',
       '建议避免自行合用，并尽快咨询医生或药师。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-WARFARIN-METRONIDAZOLE');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-HIGH-WARFARIN-AMOXICILLIN', '华法林', '阿莫西林', 'HIGH',
       '华法林与阿莫西林联用可能增强抗凝作用，增加出血风险。',
       '建议用药期间注意出血迹象，并告知药师当前所有用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-HIGH-WARFARIN-AMOXICILLIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-CLOPIDOGREL-OMEPRAZOLE-CN', '氯吡格雷', '奥美拉唑', 'MEDIUM',
       '奥美拉唑可能降低氯吡格雷抗血小板效果。',
       '建议优先咨询药师是否更换为对氯吡格雷影响更小的胃药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-CLOPIDOGREL-OMEPRAZOLE-CN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-CLOPIDOGREL-ESOMEPRAZOLE', '氯吡格雷', '埃索美拉唑', 'MEDIUM',
       '埃索美拉唑可能降低氯吡格雷抗血小板效果。',
       '建议向药师确认是否更换为泮托拉唑等替代方案。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-CLOPIDOGREL-ESOMEPRAZOLE');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-ATORVASTATIN-CLARITHROMYCIN', '阿托伐他汀', '克拉霉素', 'MEDIUM',
       '克拉霉素可升高阿托伐他汀暴露，增加肌病风险。',
       '建议咨询药师，必要时更换抗生素或暂停他汀。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-ATORVASTATIN-CLARITHROMYCIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-ATORVASTATIN-ERYTHROMYCIN', '阿托伐他汀', '红霉素', 'MEDIUM',
       '红霉素可升高阿托伐他汀血药浓度，增加肌痛和横纹肌溶解风险。',
       '建议先咨询药师，必要时调整药物组合。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-ATORVASTATIN-ERYTHROMYCIN');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-ATORVASTATIN-ITRACONAZOLE', '阿托伐他汀', '伊曲康唑', 'MEDIUM',
       '伊曲康唑可抑制阿托伐他汀代谢，增加肌病风险。',
       '建议由药师评估风险后再继续使用。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-ATORVASTATIN-ITRACONAZOLE');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-SIMVASTATIN-AMLODIPINE', '辛伐他汀', '氨氯地平', 'MEDIUM',
       '氨氯地平与辛伐他汀联用会增加辛伐他汀相关肌病风险。',
       '建议由药师确认剂量是否需要限制在每日 20mg 以内。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-SIMVASTATIN-AMLODIPINE');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-METFORMIN-CONTRAST', '二甲双胍', '造影剂', 'MEDIUM',
       '二甲双胍与含碘造影剂同用时会增加乳酸性酸中毒风险。',
       '建议在造影检查前后按医嘱暂停二甲双胍，并复查肾功能。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-METFORMIN-CONTRAST');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-ACEI-SPIRONOLACTONE', 'ACEI类', '保钾利尿剂', 'MEDIUM',
       'ACEI 类药物与保钾利尿剂联用会增加高钾血症风险。',
       '建议提交前咨询药师，并关注乏力、心悸等高钾相关症状。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-ACEI-SPIRONOLACTONE');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-ACEI-POTASSIUM', 'ACEI类', '钾补充剂', 'MEDIUM',
       'ACEI 类药物与钾补充剂联用会增加高钾血症风险。',
       '建议先咨询药师，明确是否需要联合使用和监测血钾。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-ACEI-POTASSIUM');

INSERT INTO ddi_rule (
    rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'DDI-MEDIUM-AMLODIPINE-SIMVASTATIN', '氨氯地平', '辛伐他汀', 'MEDIUM',
       '氨氯地平与辛伐他汀联用时应限制辛伐他汀剂量。',
       '建议由药师确认处方剂量是否安全。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM ddi_rule WHERE rule_code = 'DDI-MEDIUM-AMLODIPINE-SIMVASTATIN');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-WARFARIN-ACTIVE-BLEEDING', '华法林', 'DISEASE', '活动性出血', 'HIGH',
       '存在活动性出血时禁用华法林。',
       '请立即咨询医生或药师，不要自行继续用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-WARFARIN-ACTIVE-BLEEDING');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-WARFARIN-SEVERE-LIVER', '华法林', 'DISEASE', '严重肝功能不全', 'HIGH',
       '严重肝功能不全患者使用华法林时出血风险显著增加。',
       '建议由医生评估后再决定是否用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-WARFARIN-SEVERE-LIVER');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-ASPIRIN-ULCER', '阿司匹林', 'DISEASE', '活动性消化道溃疡', 'HIGH',
       '活动性消化道溃疡患者使用阿司匹林会增加出血风险。',
       '建议立即咨询药师或医生，避免自行继续使用。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-ASPIRIN-ULCER');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-ASPIRIN-ALLERGY', '阿司匹林', 'ALLERGY', '阿司匹林过敏', 'HIGH',
       '对阿司匹林过敏时禁用阿司匹林。',
       '请不要继续提交，并尽快咨询药师选择替代药物。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-ASPIRIN-ALLERGY');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-CLOPIDOGREL-ACTIVE-BLEEDING', '氯吡格雷', 'DISEASE', '活动性出血', 'HIGH',
       '存在活动性出血时禁用氯吡格雷。',
       '请先咨询医生或药师，不建议自行继续用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-CLOPIDOGREL-ACTIVE-BLEEDING');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-METFORMIN-RENAL', '二甲双胍', 'ORGAN_FUNCTION', '严重肾功能不全', 'HIGH',
       '严重肾功能不全患者使用二甲双胍会增加乳酸性酸中毒风险。',
       '建议停止自行继续用药，并尽快联系医生评估。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-METFORMIN-RENAL');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-METFORMIN-ACIDOSIS', '二甲双胍', 'DISEASE', '代谢性酸中毒', 'HIGH',
       '存在代谢性酸中毒时禁用二甲双胍。',
       '请先咨询医生处理原发问题后再评估用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-METFORMIN-ACIDOSIS');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-ACEI-BILATERAL-RENAL-ARTERY', 'ACEI类', 'DISEASE', '双侧肾动脉狭窄', 'HIGH',
       '双侧肾动脉狭窄患者使用 ACEI 类药物可能导致肾功能恶化。',
       '建议由医生确认后再用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-ACEI-BILATERAL-RENAL-ARTERY');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-ACEI-ANGIOEDEMA', 'ACEI类', 'DISEASE', '血管性水肿史', 'HIGH',
       '既往血管性水肿患者使用 ACEI 类药物存在严重风险。',
       '请立即咨询医生或药师，避免自行继续使用。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-ACEI-ANGIOEDEMA');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-BETA-BLOCKER-BRADYCARDIA', 'β受体阻滞剂', 'DISEASE', '严重心动过缓', 'HIGH',
       '严重心动过缓患者使用 β 受体阻滞剂可能加重病情。',
       '建议暂停自行继续用药并联系医生评估。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-BETA-BLOCKER-BRADYCARDIA');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-MEDIUM-BETA-BLOCKER-HEART-FAILURE', 'β受体阻滞剂', 'DISEASE', '未控制心力衰竭', 'MEDIUM',
       '未控制心力衰竭患者使用 β 受体阻滞剂需谨慎。',
       '建议提交前咨询药师或医生，确认心衰控制情况。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-MEDIUM-BETA-BLOCKER-HEART-FAILURE');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-STATIN-ACTIVE-LIVER-DISEASE', '他汀类', 'DISEASE', '活动性肝病', 'HIGH',
       '活动性肝病患者使用他汀类药物存在肝毒性风险。',
       '建议先咨询医生或药师后再决定是否继续用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-STATIN-ACTIVE-LIVER-DISEASE');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-STATIN-SEVERE-LIVER', '他汀类', 'ORGAN_FUNCTION', '严重肝功能不全', 'HIGH',
       '严重肝功能不全患者使用他汀类药物存在较高肝毒性风险。',
       '建议由医生评估后再继续用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-STATIN-SEVERE-LIVER');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-RIVAROXABAN-ACTIVE-BLEEDING', '利伐沙班', 'DISEASE', '活动性出血', 'HIGH',
       '活动性出血患者禁用利伐沙班。',
       '请立即咨询医生或药师，不建议自行继续用药。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-RIVAROXABAN-ACTIVE-BLEEDING');

INSERT INTO drug_contraindication (
    rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted
)
SELECT 'CI-HIGH-DABIGATRAN-SEVERE-RENAL', '达比加群', 'ORGAN_FUNCTION', '严重肾功能不全', 'HIGH',
       '严重肾功能不全患者使用达比加群会增加蓄积和出血风险。',
       '建议先咨询医生评估肾功能后再决定是否继续。',
       'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM drug_contraindication WHERE rule_code = 'CI-HIGH-DABIGATRAN-SEVERE-RENAL');
