#!/usr/bin/env bash
# 备份本次替换掉的原镜像到本机 docker，便于回退（不改动任何 compose）
# 用法：./scripts/backup-original-images.sh
set -euo pipefail

ORIGINALS=(
  "registry.cn-shenzhen.aliyuncs.com/hcode/hoj_backend:latest"
  "registry.cn-shenzhen.aliyuncs.com/hcode/hoj_frontend:latest"
  "registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver:latest"
  "registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database_checker:latest"
  "registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0"
)

for img in "${ORIGINALS[@]}"; do
  name=$(echo "$img" | sed -E 's#.*/([^/]+)(:.*)?#\1#')
  echo ">> pull  $img"
  docker pull "$img"
  echo ">> tag   ${name}:orig"
  docker tag "$img" "${name}:orig"
done

echo
echo "原镜像已备份为本地 :orig 标签。回退时把 compose 里的 ghcr 镜像改回原地址即可（见 scripts/rollback-to-original.sh）。"
