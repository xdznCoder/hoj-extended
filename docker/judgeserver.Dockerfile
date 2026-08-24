# syntax=docker/dockerfile:1
# 判题服务镜像：源码在 submodule source/hoj，多阶段构建 JudgeServer 模块（artifact: hoj-judgeServer）
# 运行阶段沿用官方判题镜像作为基础环境（含全部编译/沙盒工具链），只替换为我们的判题 jar
# 用法（CI 中，context=仓库根，含 submodule）：
#   docker build -f docker/judgeserver.Dockerfile -t ghcr.io/<owner>/hoj-extended-judgeserver:<tag> .

########## 构建阶段 ##########
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /build
COPY source/hoj/hoj-springboot/ /build/
RUN mvn -q clean package -pl JudgeServer -am -DskipTests -Dmaven.javadoc.skip=true

########## 运行阶段：沿用官方判题镜像（避免 EOL 基座），只换 jar ##########
FROM registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver:latest

COPY --from=build /build/JudgeServer/target/hoj-judgeServer-4.6.jar /judge/server/app.jar

WORKDIR /judge/server
ENTRYPOINT ["bash", "./check_nacos.sh"]
EXPOSE 8088
EXPOSE 5050
