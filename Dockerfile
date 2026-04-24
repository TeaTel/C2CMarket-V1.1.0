# 多阶段Docker构建 - 校园二手交易平台 v1.1.0
# 修复：使用 eclipse-temurin:21-jdk + 手动安装Maven（原 maven:3.9-eclipse-temurin-21-jdk 镜像不存在）
# DEPLOY: v1.1.0-FIX-BUILD-2026-04-24

FROM eclipse-temurin:21-jdk AS build

ARG CACHE_BUST=v1.1.0-fix-build-20260424

ENV MAVEN_VERSION=3.9.9
ENV MAVEN_HOME=/opt/maven

RUN apt-get update && apt-get install -y curl && \
    curl -sL https://dlcdn.apache.org/maven/maven-${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    ln -s ${MAVEN_HOME}/bin/mvn /usr/local/bin/mvn && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app/backend

RUN echo "Build timestamp: ${CACHE_BUST}"

COPY backend/pom.xml .

RUN mvn dependency:go-offline -B --no-transfer-progress || true

COPY backend/src ./src

RUN mvn clean package -DskipTests -B --no-transfer-progress

# ============================================
# 运行阶段 (JRE 21 Alpine)
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制后端JAR包（内含前端静态资源）
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar ./backend.jar

COPY start.sh ./start.sh
RUN chmod +x ./start.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

CMD ["./start.sh"]
