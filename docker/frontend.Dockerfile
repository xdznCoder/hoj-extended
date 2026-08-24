# syntax=docker/dockerfile:1
# 前端镜像：使用仓库内已构建好的前端产物（src/frontend/html/ 与 scrollBoard），不经 npm，稳定不卡
# 若需从源码重新构建前端，请本地执行 hoj-vue 的 npm run build 后用产物替换 src/frontend/html 再重建本镜像
# 用法（CI 中，context=仓库根，含 submodule 可选）：
#   docker build -f docker/frontend.Dockerfile -t ghcr.io/<owner>/hoj-extended-frontend:<tag> .

FROM nginx:1.15-alpine

COPY src/frontend/default.conf.template /etc/nginx/conf.d/default.conf.template
COPY src/frontend/default.conf.ssl.template /etc/nginx/conf.d/default.conf.ssl.template

# 已构建的前端产物（与原有 src/frontend/Dockerfile 一致）
ADD src/frontend/html/ /usr/share/nginx/html/
ADD src/frontend/scrollBoard/ /usr/share/nginx/scrollBoard/

COPY src/frontend/run.sh /docker-entrypoint.sh
RUN chmod a+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
EXPOSE 80
EXPOSE 443
