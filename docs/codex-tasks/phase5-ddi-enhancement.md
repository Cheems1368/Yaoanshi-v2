# Codex Task: Phase 5 DDI Enhancement — Step 5.1 + 5.2

## Task Type

骨架/批量代码型任务（协作流程 2）

```
Claude 已定约束 → Codex 施工 → Codex 自测 → Claude 审是否偏离产品
```

---

## 1. 变更文件清单（Claude 已完成的文档变更）

| 文件 | 变更说明 |
|------|---------|
| `Standard document/PRD.md` | 9.10 DDI 章节扩展：患者端 DDI 检测、用药禁忌症检查、DDI 规则管理 |
| `Standard document/BACKEND_STRUCTURE.md` | 新增 `drug_contraindication` 表、`POST /app-api/ddi/screen` 端点、admin DDI CRUD 扩展 |
| `Standard document/APP_FLOW.md` | 新增 7.4.2 患者端 DDI 告警流程、7.4.3 药师规则管理流程、`/ddi/rules` 路由 |
| `Standard document/IMPLEMENTATION_PLAN.md` | 新增 Phase 5（Step 5.1-5.4） |
| `docs/DUAL_AGENT_COLLABORATION.md` | 协作协议修订（三条铁律、四级分流、交接字段） |
| `CLAUDE.md` / `CODEX.md` | 角色定位更新 |
| `progress.txt` | 记录文档修订 |

## 2. 已执行的验证

- 所有文档变更为纯文本修订，无代码编译验证需求
- progress.txt 已更新到 2026-03-08

## 3. progress.txt 当前状态

- Phase 4 试运行收尾已完成
- DDI 增强文档已写入正式文档
- Next: Phase 5 DDI enhancement implementation Step 5.1 → 5.2 → 5.3 → 5.4

## 4. Codex 任务指令

### Task A: Step 5.1 — 卒中 DDI 规则库扩充

**目标**：生成卒中核心药物的 DDI 规则 SQL 脚本和禁忌症规则 SQL 脚本。

**约束（Claude 定义）**：

**ddi_rule 表结构**（参见 `BACKEND_STRUCTURE.md` 4.9）：
```sql
INSERT INTO ddi_rule (rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted)
VALUES ('DDI-HIGH-xxx', '药物A', '药物B', 'HIGH', '警告文案', '建议处理', 'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0);
```

**需要覆盖的药物对**（HIGH 级别，出血/严重不良反应风险）：

| # | Drug A | Drug B | 交互类型 |
|---|--------|--------|---------|
| 1 | 阿司匹林 | 华法林 | 增加出血风险（已有种子） |
| 2 | 氯吡格雷 | 华法林 | 增加出血风险 |
| 3 | 阿司匹林 | 氯吡格雷 | 双抗出血风险增加（需监测） |
| 4 | 阿司匹林 | 利伐沙班 | 增加出血风险 |
| 5 | 阿司匹林 | 达比加群 | 增加出血风险 |
| 6 | 氯吡格雷 | 利伐沙班 | 增加出血风险 |
| 7 | 氯吡格雷 | 达比加群 | 增加出血风险 |
| 8 | 替格瑞洛 | 华法林 | 增加出血风险 |
| 9 | 替格瑞洛 | 阿司匹林 | 大剂量阿司匹林降低替格瑞洛效果 |
| 10 | 华法林 | 布洛芬 | NSAIDs 增加抗凝出血风险 |
| 11 | 华法林 | 双氯芬酸 | NSAIDs 增加抗凝出血风险 |
| 12 | 阿司匹林 | 布洛芬 | NSAIDs 竞争 COX-1 降低抗血小板效果 |
| 13 | 华法林 | 阿托伐他汀 | 可能增强华法林抗凝效果 |
| 14 | 华法林 | 甲硝唑 | 增强华法林抗凝效果 |
| 15 | 华法林 | 阿莫西林 | 可能增强华法林效果 |

**需要覆盖的药物对**（MEDIUM 级别，需注意/监测）：

| # | Drug A | Drug B | 交互类型 |
|---|--------|--------|---------|
| 16 | 氯吡格雷 | 奥美拉唑 | PPI 可能降低氯吡格雷抗血小板效果（已有种子） |
| 17 | 氯吡格雷 | 艾司奥美拉唑 | PPI 可能降低氯吡格雷抗血小板效果 |
| 18 | 阿托伐他汀 | 克拉霉素 | CYP3A4 抑制增加他汀肌病风险 |
| 19 | 阿托伐他汀 | 红霉素 | CYP3A4 抑制增加他汀肌病风险 |
| 20 | 阿托伐他汀 | 伊曲康唑 | CYP3A4 抑制增加他汀肌病风险 |
| 21 | 辛伐他汀 | 氨氯地平 | 增加他汀肌病风险，辛伐他汀限剂量 |
| 22 | 二甲双胍 | 造影剂 | 乳酸酸中毒风险，需暂停 |
| 23 | ACEI类 | 保钾利尿剂 | 高钾血症风险 |
| 24 | ACEI类 | 钾补充剂 | 高钾血症风险 |
| 25 | 氨氯地平 | 辛伐他汀 | 限制辛伐他汀日剂量不超过20mg |

**drug_contraindication 表结构**（参见 `BACKEND_STRUCTURE.md` 4.9 新增）：
```sql
INSERT INTO drug_contraindication (rule_code, drug_name, condition_type, condition_value, severity, message, recommended_action, status, tenant_id, creator, create_time, updater, update_time, deleted)
VALUES ('CI-HIGH-xxx', '药物名', 'DISEASE', '禁忌条件', 'HIGH', '警告文案', '建议', 'ACTIVE', 1, 'system', NOW(), 'system', NOW(), 0);
```

**需要覆盖的禁忌症规则**：

| # | Drug | Condition Type | Condition Value | Severity | 说明 |
|---|------|---------------|-----------------|----------|------|
| 1 | 华法林 | DISEASE | 活动性出血 | HIGH | 绝对禁忌 |
| 2 | 华法林 | DISEASE | 严重肝功能不全 | HIGH | 凝血功能异常 |
| 3 | 阿司匹林 | DISEASE | 活动性消化道溃疡 | HIGH | 出血风险 |
| 4 | 阿司匹林 | ALLERGY | 阿司匹林过敏 | HIGH | 过敏禁忌 |
| 5 | 氯吡格雷 | DISEASE | 活动性出血 | HIGH | 绝对禁忌 |
| 6 | 二甲双胍 | ORGAN_FUNCTION | 严重肾功能不全 | HIGH | eGFR<30 禁用 |
| 7 | 二甲双胍 | DISEASE | 代谢性酸中毒 | HIGH | 乳酸酸中毒风险 |
| 8 | ACEI类 | DISEASE | 双侧肾动脉狭窄 | HIGH | 肾功能急剧恶化 |
| 9 | ACEI类 | DISEASE | 血管性水肿史 | HIGH | 过敏禁忌 |
| 10 | β受体阻滞剂 | DISEASE | 严重心动过缓 | HIGH | 禁用 |
| 11 | β受体阻滞剂 | DISEASE | 未控制心力衰竭 | MEDIUM | 需谨慎使用 |
| 12 | 他汀类 | DISEASE | 活动性肝病 | HIGH | 肝毒性 |
| 13 | 他汀类 | ORGAN_FUNCTION | 严重肝功能不全 | HIGH | 肝毒性 |
| 14 | 利伐沙班 | DISEASE | 活动性出血 | HIGH | 绝对禁忌 |
| 15 | 达比加群 | ORGAN_FUNCTION | 严重肾功能不全 | HIGH | CrCl<30 禁用 |

**交付物**：
1. SQL 脚本文件：`backend-v2/src/main/resources/sql/ddi-stroke-rules-seed.sql`
   - 包含所有 ddi_rule INSERT（跳过已存在的 2 条种子规则）
   - 包含 drug_contraindication 表 DDL
   - 包含所有 drug_contraindication INSERT
2. 幂等处理：使用 `INSERT IGNORE` 或 `IF NOT EXISTS` 避免重复插入

---

### Task B: Step 5.2 — 患者端 DDI 检测后端骨架

**目标**：生成患者端 DDI 检测的 service/controller/DTO/mapper 骨架代码。

**约束（Claude 定义）**：

**包结构**（参见 `CLAUDE.md` 6.1 + `CODEX.md` 7.1）：
- `com.medicine.api.controller.AppDdiController` — 患者端 DDI 端点
- `com.medicine.api.controller.AdminDdiController` — 药师端 DDI 端点（已存在，需扩展）
- `com.medicine.api.dto.ddi.AppDdiScreenRequest` — 请求 DTO
- `com.medicine.api.dto.ddi.AppDdiScreenResult` — 响应 DTO
- `com.medicine.api.dto.ddi.AppDdiAlert` — 单条告警 DTO
- `com.medicine.service.app.AppDdiScreeningService` — 患者端 DDI 检测服务
- `com.medicine.domain.ddi.DrugContraindication` — 禁忌症实体
- `com.medicine.repository.ddi.DrugContraindicationMapper` — 禁忌症 mapper

**API 合约**（参见 `BACKEND_STRUCTURE.md` 6.1）：
```
POST /app-api/ddi/screen
  Auth: Yes (patient JWT)
  Request: AppDdiScreenRequest { newDrugNames: List<String> }
  Response: CommonResult<AppDdiScreenResult>
```

**AppDdiScreenResult 结构**：
```java
public class AppDdiScreenResult {
    private List<AppDdiAlert> alerts;
}

public class AppDdiAlert {
    private String type;              // "INTERACTION" or "CONTRAINDICATION"
    private String severity;          // "HIGH" or "MEDIUM"
    private String drugA;             // existing drug or condition
    private String drugB;             // new drug
    private String message;           // warning text
    private String recommendedAction; // suggestion
}
```

**检测逻辑**（`AppDdiScreeningService.screenPatientMedications`）：
1. 从 JWT 获取 patientId
2. 通过 `MedicationPlanMapper` 获取该患者 ACTIVE 用药计划的所有 `medication_plan_item` 的 `drug_name` → existingDrugs
3. 通过 `DdiRuleMapper.selectActiveRules(tenantId)` 获取所有活跃规则，筛选 severity IN (HIGH, MEDIUM)
4. 双循环匹配：对每个 newDrug × existingDrug 组合，检查是否命中任何规则（drug_a_name/drug_b_name 双向匹配，忽略大小写）
5. 通过 `DrugContraindicationMapper.selectActiveRules(tenantId)` 获取所有活跃禁忌规则
6. 通过患者 ID 获取 `patient_health_profile` 的 disease/allergy 信息
7. 对每个 newDrug 检查是否命中任何禁忌规则（drug_name 匹配 + condition_value 在患者健康信息中存在）
8. 合并结果，按 severity 排序（HIGH 在前），返回

**Admin DDI CRUD 扩展**（已有 `AdminDdiController`，需补充）：
- `GET /admin-api/ddi/rules` — 分页查询规则（已在合约中但未实现）
- `PUT /admin-api/ddi/rules/{id}` — 编辑规则
- `GET /admin-api/ddi/contraindications` — 分页查询禁忌规则
- `POST /admin-api/ddi/contraindications` — 新增禁忌规则
- `PUT /admin-api/ddi/contraindications/{id}` — 编辑禁忌规则

**交付物**：
1. 所有上述 Java 文件的骨架代码
2. `DrugContraindication` 实体 + `DrugContraindicationMapper`
3. `AppDdiScreeningService` 完整实现
4. `AdminDdiController` CRUD 扩展
5. 单元测试：`AppDdiScreeningServiceTest`
6. DDL：`drug_contraindication` 表创建语句追加到 `app-auth-patient-local-schema.sql`

---

## 5. 依据的正式文档/章节

| 文档 | 相关章节 |
|------|---------|
| `Standard document/BACKEND_STRUCTURE.md` | 4.9 DDI domain（ddi_rule, ddi_screening_log, drug_contraindication 表定义） |
| `Standard document/BACKEND_STRUCTURE.md` | 6.1 App APIs — DDI screening (patient-side) |
| `Standard document/BACKEND_STRUCTURE.md` | 6.2 Admin APIs — DDI and notification |
| `Standard document/PRD.md` | 9.10 药物相互作用 DDI（患者端 + 禁忌症 + 规则管理） |
| `Standard document/IMPLEMENTATION_PLAN.md` | Phase 5 Step 5.1 + 5.2 |
| `CODEX.md` | 7.1 Backend rules（包结构、命名） |

## 6. 验证命令

完成后请执行：

```bash
# 编译验证
mvn -q -DskipTests compile -f D:/yaoanshi/backend-v2/pom.xml

# 单元测试
mvn -q "-Dtest=AppDdiScreeningServiceTest" test -f D:/yaoanshi/backend-v2/pom.xml

# 全量测试回归
mvn -q test -f D:/yaoanshi/backend-v2/pom.xml
```

## 7. 完成后交回 Claude 时需提供

1. 生成的代码文件清单
2. 编译和测试结果
3. 发现的问题列表（如有）
4. 依据的正式文档/章节
5. 更新后的 progress.txt 状态
