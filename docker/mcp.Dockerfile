# syntax=docker/dockerfile:1
# MCP 服务镜像：JDK21 + Spring Boot 3 + Spring AI MCP
# 用法（CI，context=仓库根）：docker build -f docker/mcp.Dockerfile -t ghcr.io/<owner>/hoj-extended-mcp:<tag> .

########## 构建阶段 ##########
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY mcp-server/pom.xml ./
# 先预取依赖（层缓存）
RUN mvn -q -B -DskipTests dependency:go-offline || true
COPY mcp-server/src ./src
RUN mvn -q -B -DskipTests package

########## 运行阶段 ##########
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/hoj-mcp-server-0.1.0.jar /app/app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 3000
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
