# syntax=docker/dockerfile:1
# 前端镜像：源码在 submodule source/hoj（hoj-vue），多阶段构建，最终为 nginx 镜像
# 用法（CI 中，context=仓库根，含 submodule）：
#   docker build -f docker/frontend.Dockerfile -t ghcr.io/<owner>/hoj-extended-frontend:<tag> .

########## 构建阶段 ##########
FROM node:16-alpine AS fe
WORKDIR /app
# 先拷 package.json/lock，利用层缓存（若 lock 不存在则回退 install）
COPY source/hoj/hoj-vue/package.json source/hoj/hoj-vue/package-lock.json* ./
COPY source/hoj/hoj-vue/ ./
RUN npm config set registry https://registry.npmmirror.com && \
    rm -f package-lock.json && \
    npm install --no-audit --no-fund && \
    npm run build

########## 运行阶段 ##########
FROM nginx:1.15-alpine

COPY src/frontend/default.conf.template /etc/nginx/conf.d/default.conf.template
COPY src/frontend/default.conf.ssl.template /etc/nginx/conf.d/default.conf.ssl.template

# 前端构建产物
COPY --from=fe /app/dist /usr/share/nginx/html/

# 榜单页面等静态资源沿用仓库内构建产物
COPY src/frontend/scrollBoard /usr/share/nginx/scrollBoard/

COPY src/frontend/run.sh /docker-entrypoint.sh
RUN chmod a+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80
EXPOSE 443
