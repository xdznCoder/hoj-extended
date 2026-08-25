#!/usr/bin/env bash
# 把 standAlone / distributed 的 compose 里的 ghcr 镜像改回原 aliyun 镜像（回退用）
# 用法：./scripts/rollback-to-original.sh   然后 docker compose pull && up -d
set -euo pipefail
cd "$(dirname "$0")/.."

sed -i \
  -e 's#ghcr.io/xdzncoder/hoj-extended-backend:latest#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_backend#g' \
  -e 's#ghcr.io/xdzncoder/hoj-extended-frontend:latest#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend#g' \
  -e 's#ghcr.io/xdzncoder/hoj-extended-judgeserver:latest#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver#g' \
  -e 's#ghcr.io/xdzncoder/hoj-extended-mysql-checker:latest#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database_checker#g' \
  -e 's#ghcr.io/xdzncoder/hoj-extended-rsync:latest#registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0#g' \
  standAlone/docker-compose.yml distributed/main/docker-compose.yml distributed/judgeserver/docker-compose.yml

echo "已改回原镜像。执行：docker compose pull && docker compose up -d"
echo "（此修改可 git checkout -- standAlone distributed 撤销）"
