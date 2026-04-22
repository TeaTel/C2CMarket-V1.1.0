# 单阶段Docker构建 - 校园二手交易平台
# 策略：前端已预构建（dist目录），只需处理后端

FROM maven:3.9-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend

# 复制pom.xml
COPY backend/pom.xml .

# 下载依赖
RUN mvn dependency:go-offline -B --no-transfer-progress || true

# 复制后端源码
COPY backend/src ./src

# 构建JAR包
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ============================================
# 运行阶段 (JRE 21 Alpine)
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制前端预构建产物（已在本地构建好）
COPY frontend/dist ./static

# 复制后端JAR包
COPY --from=backend-build /app/backend/target/backend-0.0.1-SNAPSHOT.jar ./backend.jar

# 复制启动脚本
COPY start.sh ./start.sh
RUN chmod +x ./start.sh

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

CMD ["./start.sh"]
