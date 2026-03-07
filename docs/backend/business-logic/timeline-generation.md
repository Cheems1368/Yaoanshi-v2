# 时间线生成算法

## 概述

时间线功能为工单提供完整的处理过程追踪，基于工单状态和操作记录自动生成时间线事件。

## 时间线事件类型

```typescript
type TimelineEvent =
    | 'SUBMITTED'           // 提交
    | 'ASSIGNED_PHARMACIST' // 分配药师
    | 'TO_DOCTOR'           // 转交医生
    | 'DOCTOR_REVIEW'       // 医生审核
    | 'PRESCRIPTION_READY'   // 处方就绪
    | 'DONE'                // 完成
    | 'REJECTED';           // 驳回
```

## 时间线生成逻辑

### 核心算法

```typescript
async findOneWithTimeline(id: Id) {
    const order = await this.mustGet(id);
    const timeline: Array<{ at: string; event: TimelineEvent; by?: Id; to?: Id }> = [];
    
    // 1. 提交时间线
    if (order.createdAt) {
        timeline.push({
            at: this.toISOString(order.createdAt),
            event: 'SUBMITTED',
            by: order.patientId,
        });
    }
    
    // 2. 药师认领时间线
    const pharmacistId = order.pharmacistId ?? order.assignedPharmacistId;
    if (pharmacistId) {
        const at = order.assignedAt ?? order.updatedAt ?? order.createdAt;
        timeline.push({
            at: this.toISOString(at),
            event: 'ASSIGNED_PHARMACIST',
            by: pharmacistId,
        });
    }
    
    // 3. 升级医生时间线
    const doctorId = order.doctorId;
    if (doctorId || order.status === 'TO_DOCTOR' || order.status === 'DOCTOR_REVIEW') {
        const at = order.escalatedAt ?? order.updatedAt ?? order.createdAt;
        timeline.push({
            at: this.toISOString(at),
            event: order.status === 'DOCTOR_REVIEW' ? 'DOCTOR_REVIEW' : 'TO_DOCTOR',
            by: pharmacistId ?? undefined,
            to: doctorId ?? undefined,
        });
    }
    
    // 4. 医生处理完成时间线
    if (order.status === 'PRESCRIPTION_READY' || order.status === 'DONE') {
        const at = order.prescriptionReadyAt ?? order.doneAt ?? order.updatedAt ?? order.createdAt;
        timeline.push({
            at: this.toISOString(at),
            event: order.status === 'DONE' ? 'DONE' : 'PRESCRIPTION_READY',
            by: doctorId ?? undefined,
        });
    }
    
    // 5. 驳回时间线
    if (order.status === 'REJECTED') {
        const at = order.rejectedAt ?? order.updatedAt ?? order.createdAt;
        timeline.push({
            at: this.toISOString(at),
            event: 'REJECTED',
            by: doctorId ?? pharmacistId ?? undefined,
        });
    }
    
    return {
        ...order,
        pharmacistId,
        doctorId,
        timeline,
    };
}
```

## 时间戳优先级

### 时间戳字段优先级
1. **专用时间戳字段** (如 `assignedAt`, `escalatedAt`)
2. **更新时间** (`updatedAt`)
3. **创建时间** (`createdAt`)
4. **当前时间** (兜底)

### 时间格式化
```typescript
private toISOString(v?: any): string {
    if (!v) return new Date().toISOString();
    if (v instanceof Date) return v.toISOString();
    const d = new Date(v);
    return isNaN(+d) ? String(v) : d.toISOString();
}
```

## 时间线数据结构

### 时间线事件对象
```typescript
interface TimelineEvent {
    at: string;        // ISO 8601 时间格式
    event: string;     // 事件类型
    by?: number;       // 操作人ID
    to?: number;       // 目标人ID（如分配时）
}
```

### 完整响应结构
```typescript
interface HelpOrderWithTimeline {
    id: number;
    patientId: number;
    assignedPharmacistId?: number;
    assignedDoctorId?: number;
    title: string;
    description: string;
    status: string;
    createdAt: Date;
    updatedAt: Date;
    timeline: TimelineEvent[];
}
```

## 扩展字段建议

### 需要添加的时间戳字段
- `assignedAt`: 药师分配时间
- `escalatedAt`: 升级医生时间
- `prescriptionReadyAt`: 处方就绪时间
- `doneAt`: 完成时间
- `rejectedAt`: 驳回时间

### 需要添加的关联字段
- `assignedDoctorId`: 分配医生ID
- `rejectedBy`: 驳回人ID
- `rejectReason`: 驳回原因

## 实现要点

### 1. 兼容性处理
- 兼容不同的字段命名（`pharmacistId` vs `assignedPharmacistId`）
- 处理缺失的时间戳字段
- 优雅降级到默认时间

### 2. 性能优化
- 时间线生成不依赖数据库查询
- 基于内存中的工单数据生成
- 避免N+1查询问题

### 3. 扩展性设计
- 支持自定义时间线事件
- 支持事件过滤和排序
- 支持时间线分页
