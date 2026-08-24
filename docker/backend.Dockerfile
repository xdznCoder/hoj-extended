# syntax=docker/dockerfile:1
# 后端镜像：源码在 submodule source/hoj，多阶段构建 DataBackup 模块（artifact: hoj-backend）
# 用法（CI 中，context=仓库根，含 submodule）：
#   docker build -f docker/backend.Dockerfile -t ghcr.io/<owner>/hoj-extended-backend:<tag> .

########## 构建阶段 ##########
FROM maven:3.8-openjdk-8 AS build
WORKDIR /build

# 复制 submodule 里的源码（hoj-springboot 整个多模块工程，含 api 依赖）
COPY source/hoj/hoj-springboot/ /build/

# 仅编译后端模块 DataBackup，并带上它依赖的 api 模块（-am）
RUN mvn -q clean package -pl DataBackup -am -DskipTests -Dmaven.javadoc.skip=true

########## 运行阶段 ##########
FROM openjdk:8
ENV TZ=Asia/Shanghai
ENV BACKEND_SERVER_PORT=6688
VOLUME ["/hoj/file", "/hoj/testcase"]

# 后端 Spring Boot 产物
COPY --from=build /build/DataBackup/target/hoj-backend-4.6.jar /app.jar

COPY src/backend/run.sh /run.sh
COPY src/backend/check_nacos.sh /check_nacos.sh

RUN chmod a+x /run.sh /check_nacos.sh && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

CMD ["bash", "/check_nacos.sh"]
EXPOSE $BACKEND_SERVER_PORT
