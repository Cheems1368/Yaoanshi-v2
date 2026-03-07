# 故障排查

本目录包含故障排查相关文档。

## 📋 文档列表

| 文档 | 描述 |
|------|------|
| [故障排查手册.md](故障排查手册.md) | 常见故障排查方法和解决方案 |

## 🔍 快速诊断

### 服务状态检查
```bash
# 检查所有服务状态
docker compose ps

# 检查服务健康状态
curl http://localhost:48080/actuator/health
```

### 日志查看
```bash
# 查看实时日志
docker logs -f medicine-server

# 查看错误日志
docker logs medicine-server 2>&1 | grep -i error
```

## 🔗 相关文档

- [运维部署](../03-运维部署/)
- [开发指南](../02-开发指南/)
- [测试文档](../06-测试文档/)

