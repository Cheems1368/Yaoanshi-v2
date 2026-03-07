# 安全扫描报告索引

本目录用于存放使用 Trivy 扫描生成的报告（原始输出与摘要）。

- 报告生成位置：`trivy-reports/<时间戳>/`
- 原始输出文件：`trivy_<类型>_<目标>.(txt|json|sarif|cyclonedx)`
- 摘要文件：`SUMMARY_<类型>_<目标>.md`

示例：

- `trivy-reports/20250127_160501/trivy_image_medicine-server.optimized.txt`
- `trivy-reports/20250127_160501/SUMMARY_image_medicine-server.optimized.md`

使用脚本：

```powershell
powershell -ExecutionPolicy Bypass -File docker-security-audit/docker-security-scan-trivy.ps1 -TargetType image -TargetName medicine-server:optimized -Severity CRITICAL,HIGH -IgnoreUnfixed -Format table
```
