#!/usr/bin/env bash

set -euo pipefail

BASE_DIR="${1:-/app}"
BRANCH="${BRANCH:-main}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
UPDATE_SCRIPT="${SCRIPT_DIR}/update-repos.sh"

if [[ ! -x "${UPDATE_SCRIPT}" ]]; then
  echo "ERROR: update script not found or not executable: ${UPDATE_SCRIPT}" >&2
  exit 1
fi

echo "==> Updating repositories into ${BASE_DIR}"
"${UPDATE_SCRIPT}" "${BASE_DIR}"

if [[ ! -f "${BASE_DIR}/.env" ]]; then
  cp "${DEPLOY_DIR}/.env.example" "${BASE_DIR}/.env"
  echo "==> Created ${BASE_DIR}/.env from deploy/.env.example"
  echo "==> Please edit ${BASE_DIR}/.env and rerun deploy if secrets are still empty"
fi

cp "${DEPLOY_DIR}/.env.example" "${BASE_DIR}/.env.example"
echo "==> Synced .env.example to ${BASE_DIR}"

COMPOSE_FILE="${BASE_DIR}/global-stock-webapi/deploy/docker-compose.yml"

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "ERROR: compose file not found: ${COMPOSE_FILE}" >&2
  exit 1
fi

cd "${BASE_DIR}/global-stock-webapi/deploy"

echo "==> Pulling latest base images"
docker compose --env-file "${BASE_DIR}/.env" -f "${COMPOSE_FILE}" pull || true

echo "==> Building and starting services"
docker compose --env-file "${BASE_DIR}/.env" -f "${COMPOSE_FILE}" up -d --build

echo "==> Deployment status"
docker compose --env-file "${BASE_DIR}/.env" -f "${COMPOSE_FILE}" ps
