# 工单状态流转逻辑

## 状态定义

```typescript
export type HelpOrderStatus =
    | 'SUBMITTED'           // 已提交
    | 'ASSIGNED_PHARMACIST' // 已分配药师
    | 'PHARMACIST_REVIEW'   // 药师审核中
    | 'ADVICE_READY'        // 建议已就绪
    | 'TO_DOCTOR'           // 转交医生
    | 'DOCTOR_REVIEW'       // 医生审核中
    | 'PRESCRIPTION_READY'  // 处方已就绪
    | 'DONE'                // 已完成
    | 'REJECTED';           // 已驳回
```

## 状态转换规则

### 1. 初始状态
- **SUBMITTED**: 患者提交工单后的初始状态

### 2. 药师处理流程
- **SUBMITTED → ASSIGNED_PHARMACIST**: 系统分配或药师主动认领
- **ASSIGNED_PHARMACIST → PHARMACIST_REVIEW**: 药师开始处理工单
- **PHARMACIST_REVIEW → ADVICE_READY**: 药师完成建议
- **PHARMACIST_REVIEW → TO_DOCTOR**: 需要医生介入

### 3. 医生处理流程
- **TO_DOCTOR → DOCTOR_REVIEW**: 医生开始处理
- **DOCTOR_REVIEW → PRESCRIPTION_READY**: 医生开具处方
- **PRESCRIPTION_READY → DONE**: 患者确认完成

### 4. 异常流程
- **任意状态 → REJECTED**: 工单被驳回

## 业务逻辑实现

### 药师认领逻辑
```typescript
async assignToPharmacist(id: Id, pharmacistId: Id) {
    const order = await this.mustGet(id);
    
    // 分配药师
    order.assignedPharmacistId = pharmacistId;
    order.assignedAt = new Date();
    
    // 状态转换
    if (order.status === 'SUBMITTED') {
        order.status = 'ASSIGNED_PHARMACIST';
    } else if (order.status === 'ASSIGNED_PHARMACIST') {
        order.status = 'PHARMACIST_REVIEW';
    }
    
    return await this.repo.save(order);
}
```

### 升级医生逻辑
```typescript
async escalateToDoctor(id: Id, payload: { needDoctor: boolean; doctorId?: Id }) {
    const { needDoctor, doctorId } = payload;
    const order = await this.mustGet(id);
    
    if (needDoctor) {
        order.doctorId = doctorId;
        order.escalatedAt = new Date();
        order.status = 'TO_DOCTOR';
    }
    
    return await this.repo.save(order);
}
```

## 权限控制

### 角色权限
- **患者**: 只能查看自己的工单，创建工单
- **药师**: 处理分配给自己的工单，升级到医生
- **医生**: 处理需要医生审核的工单
- **管理员**: 查看所有工单，分配工单

### 状态权限
- 只有当前处理人才能修改工单状态
- 状态转换必须符合业务规则
- 异常状态需要记录操作日志
