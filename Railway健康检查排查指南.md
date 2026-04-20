# Railway健康检查排查指南

## 问题描述
项目在Railway部署中无法通过健康检查，错误信息：`Path: /actuator/health ... Healthcheck failed!`

## 需要的Railway日志信息

为了全面排查健康检查失败的问题，需要从Railway获取以下日志信息：

### 1. Build Logs（构建日志）
**获取方式**：Railway Dashboard → 项目 → Deployments → 点击失败的部署 → Build Logs

**需要关注的信息**：
- Maven构建是否成功
- 是否有编译错误
- JAR文件是否成功生成
- 依赖下载是否正常
- 构建时间统计

**关键检查点**：
```
[INFO] Building jar: /app/backend/target/backend-0.0.1-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

### 2. Deploy Logs（部署日志）
**获取方式**：Railway Dashboard → 项目 → Deployments → 点击失败的部署 → Deploy Logs

**需要关注的信息**：
- 容器启动过程
- 环境变量设置
- 启动脚本执行情况
- Java版本信息
- 应用启动日志
- 数据库连接尝试

**关键检查点**：
```
Starting Campus Market Backend...
Environment variables:
MYSQLHOST: xxx.railway.internal
MYSQLPORT: 3306
MYSQLDATABASE: railway
MYSQLUSER: root
MYSQLPASSWORD: *****
Starting Spring Boot application...
```

### 3. HTTP Logs（HTTP请求日志）
**获取方式**：Railway Dashboard → 项目 → Metrics → HTTP Logs

**需要关注的信息**：
- 健康检查请求记录
- 响应状态码
- 请求时间戳
- 响应时间
- 错误信息

**关键检查点**：
```
GET /actuator/health 200 OK
GET /actuator/health 503 Service Unavailable
GET /actuator/health 404 Not Found
```

### 4. NetworkFlow Logs（网络流量日志）
**获取方式**：Railway Dashboard → 项目 → Metrics → Network Flow

**需要关注的信息**：
- 容器网络连接状态
- 数据库连接尝试
- 端口监听情况
- 网络超时错误

### 5. 环境变量配置
**获取方式**：Railway Dashboard → 项目 → Variables

**需要检查的环境变量**：
- `MYSQLHOST` - MySQL主机地址
- `MYSQLPORT` - MySQL端口（默认3306）
- `MYSQLDATABASE` - 数据库名称
- `MYSQLUSER` - 数据库用户名
- `MYSQLPASSWORD` - 数据库密码
- `SPRING_PROFILES_ACTIVE` - 应该设置为`prod`
- `JWT_SECRET` - JWT密钥
- `PORT` - 应用端口（Railway自动设置）

## 常见问题及解决方案

### 问题1：数据库连接失败
**症状**：
- 应用启动时数据库连接超时
- 健康检查返回`DOWN`状态
- 日志显示`Communications link failure`

**解决方案**：
1. 检查Railway MySQL服务状态
2. 验证环境变量名称是否正确（Railway使用`MYSQL*`前缀）
3. 检查连接超时设置（已配置为60秒）
4. 验证数据库用户权限

### 问题2：Actuator端点无法访问
**症状**：
- 健康检查返回404
- 安全配置阻止访问
- 端点未启用

**解决方案**：
1. 已修复：SecurityConfig允许`/actuator/health`匿名访问
2. 已修复：application.yml中启用了Actuator端点
3. 检查启动脚本中的Spring Profile设置

### 问题3：应用启动超时
**症状**：
- 健康检查在120秒后超时
- 应用启动时间过长
- 数据库初始化耗时

**解决方案**：
1. 已配置：`healthcheckTimeout: 120`
2. 已配置：数据库连接池优化
3. 已配置：延迟初始化`lazy-initialization: true`

### 问题4：端口配置错误
**症状**：
- 应用监听错误端口
- Railway无法连接到应用
- 健康检查无法到达

**解决方案**：
1. 应用配置：`server.port: ${PORT:8080}`
2. Railway自动设置`PORT`环境变量
3. 启动脚本使用`${PORT:8080}`

## 排查步骤

### 步骤1：检查构建日志
1. 登录Railway Dashboard
2. 进入项目 → Deployments
3. 点击最新的失败部署
4. 查看Build Logs，确认构建成功

### 步骤2：检查部署日志
1. 在同一部署页面查看Deploy Logs
2. 查找错误信息
3. 检查环境变量是否正确显示
4. 查看应用启动过程

### 步骤3：检查HTTP日志
1. 进入项目 → Metrics → HTTP Logs
2. 过滤`/actuator/health`请求
3. 查看响应状态码
4. 分析错误模式

### 步骤4：验证环境变量
1. 进入项目 → Variables
2. 确认所有必需变量已设置
3. 检查变量值是否正确
4. 特别注意`SPRING_PROFILES_ACTIVE=prod`

### 步骤5：手动测试健康检查
如果应用已启动但健康检查失败：
1. 获取Railway应用URL
2. 使用curl测试：
   ```bash
   curl https://your-app.railway.app/actuator/health
   ```
3. 查看详细健康信息：
   ```bash
   curl https://your-app.railway.app/actuator/health | jq .
   ```

## 已实施的修复

### 1. Actuator配置修复
- ✅ 启用了Spring Boot Actuator端点
- ✅ 配置了健康检查详细信息
- ✅ 添加了数据库健康检查组件

### 2. 安全配置修复
- ✅ 允许匿名访问`/actuator/health`
- ✅ 允许匿名访问`/actuator/info`
- ✅ 配置了CORS支持

### 3. 数据库配置优化
- ✅ 使用Railway的`MYSQL*`环境变量
- ✅ 配置了连接池和超时设置
- ✅ 添加了数据库健康检查

### 4. 启动脚本优化
- ✅ 添加了环境变量调试信息
- ✅ 配置了详细的日志级别
- ✅ 添加了JAR文件检查

## 如果问题仍然存在

### 1. 提供以下日志信息：
- 完整的Build Logs（截图或文本）
- 完整的Deploy Logs（截图或文本）
- HTTP Logs中关于`/actuator/health`的条目
- 环境变量列表（隐藏敏感信息）

### 2. 本地测试验证：
```bash
# 在本地测试生产配置
cd env-setup/backend
SPRING_PROFILES_ACTIVE=prod \
MYSQLHOST=localhost \
MYSQLPORT=3306 \
MYSQLDATABASE=test \
MYSQLUSER=root \
MYSQLPASSWORD=123456 \
java -jar target/backend-0.0.1-SNAPSHOT.jar

# 测试健康检查
curl http://localhost:8080/actuator/health
```

### 3. 联系Railway支持：
如果所有配置都正确但问题仍然存在，可能需要联系Railway技术支持，提供：
- 项目ID
- 部署ID
- 详细的错误日志
- 环境配置信息

## 紧急解决方案

如果急需部署，可以暂时禁用健康检查：

1. 修改`railway.json`：
```json
{
  "deploy": {
    "healthcheckPath": null,
    "healthcheckTimeout": null
  }
}
```

2. 重新部署项目

**注意**：这只是临时解决方案，生产环境应确保健康检查正常工作。

---

**最后更新**：2026年4月17日  
**状态**：等待Railway日志信息进行进一步分析  
**建议**：请提供Railway的Build Logs和Deploy Logs以便深入排查