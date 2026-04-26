# 全栈Docker构建 - 校园二手交易平台 v1.1.1
# 构建顺序: 前端(Vite) → 后端(Maven) → 运行(JRE)
# DEPLOY: v1.1.1-FORCE-REBUILD-2026-04-26

FROM eclipse-temurin:21-jdk AS build

ARG CACHE_BUST=v1.1.1-force-rebuild-api-fix-20260426-1730

ENV MAVEN_VERSION=3.9.9
ENV MAVEN_HOME=/opt/maven
ENV NODE_VERSION=20
ENV NPM_CONFIG_REGISTRY=https://registry.npmmirror.com

RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    curl -fSL -o /tmp/maven.tar.gz "https://archive.apache.org/dist/maven/maven-${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    tar xzf /tmp/maven.tar.gz -C /opt && rm /tmp/maven.tar.gz && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    ln -s ${MAVEN_HOME}/bin/mvn /usr/local/bin/mvn && \
    curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - && \
    apt-get install -y nodejs && \
    apt-get purge -y curl && apt-get autoremove -y && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN echo "Build timestamp: ${CACHE_BUST}"

COPY frontend/package.json frontend/package-lock.json* /app/frontend/
RUN cd /app/frontend && npm ci --prefer-offline 2>/dev/null || npm install

COPY frontend/ /app/frontend/

RUN cd /app/frontend && npm run build

WORKDIR /app/backend

COPY backend/pom.xml .

RUN mvn dependency:go-offline -B --no-transfer-progress || true

COPY backend/src ./src

RUN cp -r /app/frontend/dist/* src/main/resources/static/ 2>/dev/null; \
    mkdir -p src/main/resources/static && \
    cp -r /app/frontend/dist/* src/main/resources/static/ 2>/dev/null || true

RUN mvn clean package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar ./backend.jar

COPY start.sh ./start.sh
RUN chmod +x ./start.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

CMD ["./start.sh"]
