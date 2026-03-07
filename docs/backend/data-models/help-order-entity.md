# 工单实体数据模型

## 实体定义

### TypeScript 实体 (NestJS)

```typescript
@Entity('help_orders')
export class HelpOrder {
    @PrimaryGeneratedColumn('increment') 
    id: number;
    
    @Column() 
    patientId: number;
    
    @Column({ nullable: true }) 
    assignedPharmacistId: number;
    
    @Column({ type: 'varchar', length: 128 }) 
    title: string;
    
    @Column({ type: 'text' }) 
    description: string;
    
    @Column({ type: 'json', nullable: true }) 
    intendedDrugs: string[];
    
    @Column({ type: 'json', nullable: true }) 
    attachments: string[];
    
    @Column({ default: false }) 
    toDoctor: boolean;
    
    @Column({ 
        type: 'enum', 
        enum: [
            'SUBMITTED','ASSIGNED_PHARMACIST','PHARMACIST_REVIEW',
            'ADVICE_READY','TO_DOCTOR','DOCTOR_REVIEW',
            'PRESCRIPTION_READY','DONE','REJECTED'
        ] 
    })
    status: HelpOrderStatus;
    
    @CreateDateColumn() 
    createdAt: Date;
    
    @UpdateDateColumn() 
    updatedAt: Date;
}
```

### Java 实体 (Spring Boot)

```java
@Entity
@Table(name = "help_orders")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HelpOrderDO extends BaseDO {
    
    /**
     * 工单编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 患者编号
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    /**
     * 分配药师编号
     */
    @Column(name = "assigned_pharmacist_id")
    private Long assignedPharmacistId;
    
    /**
     * 分配医生编号
     */
    @Column(name = "assigned_doctor_id")
    private Long assignedDoctorId;
    
    /**
     * 工单标题
     */
    @Column(name = "title", length = 128, nullable = false)
    private String title;
    
    /**
     * 工单描述
     */
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    /**
     * 意向药品
     */
    @Column(name = "intended_drugs", columnDefinition = "JSON")
    private String intendedDrugs;
    
    /**
     * 附件列表
     */
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;
    
    /**
     * 是否需要医生
     */
    @Column(name = "to_doctor", nullable = false)
    private Boolean toDoctor = false;
    
    /**
     * 工单状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HelpOrderStatus status = HelpOrderStatus.SUBMITTED;
    
    /**
     * 分配时间
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    /**
     * 升级时间
     */
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    /**
     * 处方就绪时间
     */
    @Column(name = "prescription_ready_at")
    private LocalDateTime prescriptionReadyAt;
    
    /**
     * 完成时间
     */
    @Column(name = "done_at")
    private LocalDateTime doneAt;
    
    /**
     * 驳回时间
     */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    /**
     * 驳回人
     */
    @Column(name = "rejected_by")
    private Long rejectedBy;
    
    /**
     * 驳回原因
     */
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
}
```

## 状态枚举

### Java 枚举定义

```java
@Getter
@AllArgsConstructor
public enum HelpOrderStatus {
    
    SUBMITTED("SUBMITTED", "已提交"),
    ASSIGNED_PHARMACIST("ASSIGNED_PHARMACIST", "已分配药师"),
    PHARMACIST_REVIEW("PHARMACIST_REVIEW", "药师审核中"),
    ADVICE_READY("ADVICE_READY", "建议已就绪"),
    TO_DOCTOR("TO_DOCTOR", "转交医生"),
    DOCTOR_REVIEW("DOCTOR_REVIEW", "医生审核中"),
    PRESCRIPTION_READY("PRESCRIPTION_READY", "处方已就绪"),
    DONE("DONE", "已完成"),
    REJECTED("REJECTED", "已驳回");
    
    private final String code;
    private final String name;
    
    public static HelpOrderStatus fromCode(String code) {
        for (HelpOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
```

## 数据库表结构

### MySQL 建表语句

```sql
CREATE TABLE help_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单编号',
    patient_id BIGINT NOT NULL COMMENT '患者编号',
    assigned_pharmacist_id BIGINT COMMENT '分配药师编号',
    assigned_doctor_id BIGINT COMMENT '分配医生编号',
    title VARCHAR(128) NOT NULL COMMENT '工单标题',
    description TEXT NOT NULL COMMENT '工单描述',
    intended_drugs JSON COMMENT '意向药品',
    attachments JSON COMMENT '附件列表',
    to_doctor BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要医生',
    status ENUM(
        'SUBMITTED','ASSIGNED_PHARMACIST','PHARMACIST_REVIEW',
        'ADVICE_READY','TO_DOCTOR','DOCTOR_REVIEW',
        'PRESCRIPTION_READY','DONE','REJECTED'
    ) NOT NULL DEFAULT 'SUBMITTED' COMMENT '工单状态',
    assigned_at DATETIME COMMENT '分配时间',
    escalated_at DATETIME COMMENT '升级时间',
    prescription_ready_at DATETIME COMMENT '处方就绪时间',
    done_at DATETIME COMMENT '完成时间',
    rejected_at DATETIME COMMENT '驳回时间',
    rejected_by BIGINT COMMENT '驳回人',
    reject_reason TEXT COMMENT '驳回原因',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_assigned_pharmacist_id (assigned_pharmacist_id),
    INDEX idx_assigned_doctor_id (assigned_doctor_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';
```

## 字段说明

### 核心字段
- `id`: 工单唯一标识
- `patientId`: 患者ID，关联患者表
- `assignedPharmacistId`: 分配药师ID，关联用户表
- `assignedDoctorId`: 分配医生ID，关联用户表
- `title`: 工单标题，最大128字符
- `description`: 工单详细描述，TEXT类型
- `status`: 工单状态，枚举类型

### 扩展字段
- `intendedDrugs`: 意向药品列表，JSON格式
- `attachments`: 附件文件列表，JSON格式
- `toDoctor`: 是否需要医生介入，布尔值

### 时间戳字段
- `assignedAt`: 药师分配时间
- `escalatedAt`: 升级医生时间
- `prescriptionReadyAt`: 处方就绪时间
- `doneAt`: 完成时间
- `rejectedAt`: 驳回时间

### 审计字段
- `rejectedBy`: 驳回人ID
- `rejectReason`: 驳回原因
- `creator`: 创建者
- `createTime`: 创建时间
- `updater`: 更新者
- `updateTime`: 更新时间
- `deleted`: 软删除标记
- `tenantId`: 租户ID（多租户支持）

## 索引设计

### 主要索引
- `idx_patient_id`: 患者查询索引
- `idx_assigned_pharmacist_id`: 药师查询索引
- `idx_assigned_doctor_id`: 医生查询索引
- `idx_status`: 状态查询索引
- `idx_create_time`: 时间排序索引
- `idx_tenant_id`: 租户查询索引

### 复合索引建议
- `idx_patient_status`: (patient_id, status) - 患者工单状态查询
- `idx_pharmacist_status`: (assigned_pharmacist_id, status) - 药师工单状态查询
- `idx_doctor_status`: (assigned_doctor_id, status) - 医生工单状态查询
