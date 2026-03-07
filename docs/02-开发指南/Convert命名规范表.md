# Convert 命名规范表

| Convert 类             | 允许的方法（只在此列表内）                         | 备注 |
|-----------------------|----------------------------------------------------|------|
| MedCheckinConvert     | toDO(reqDTO) / toRespVO(do) / convertList(dos) / convertPage(pageDO) | 禁止写查询/远程调用 |
| MedNotificationConvert| toDO(reqDTO) / toRespVO(do) / convertList(dos)     |      |
| MedReportConvert      | toDTO(reqVO) / toRespVO(dto) / convertPage(pageDTO)|      |
| MedPlanConvert        | toDO(reqDTO) / toRespVO(do) / convertList(dos) / convertPage(pageDO) |      |
| MedAdherenceStatsConvert | toDO(reqDTO) / toRespVO(do) / convertList(dos) |      |
| TodayTaskConvert      | toDTO(reqVO) / toRespVO(dto) / convertList(dtos) |      |
| ReminderTaskConvert   | toDO(reqDTO) / toRespVO(do) / convertList(dos) |      |

## 方法命名规范

### 基础转换方法
- `toDO(reqDTO)` - 请求DTO转DO
- `toRespVO(do)` - DO转响应VO
- `toDTO(reqVO)` - 请求VO转DTO
- `toRespVO(dto)` - DTO转响应VO

### 集合转换方法
- `convertList(dos)` - DO列表转DTO列表
- `convertList(dtos)` - DTO列表转VO列表
- `convertPage(pageDO)` - DO分页转DTO分页
- `convertPage(pageDTO)` - DTO分页转VO分页

### 特殊转换方法
- `stringToEvidenceDTO(evidenceJson)` - 字符串转EvidenceDTO（自定义映射）

## 禁止事项
- ❌ 禁止在Convert中调用Service
- ❌ 禁止在Convert中执行数据库查询
- ❌ 禁止在Convert中包含业务逻辑
- ❌ 禁止在Convert中调用外部API

## 推荐事项
- ✅ 使用MapStruct注解处理器
- ✅ 复杂转换使用@Named方法
- ✅ 保持方法单一职责
- ✅ 添加适当的注释说明
