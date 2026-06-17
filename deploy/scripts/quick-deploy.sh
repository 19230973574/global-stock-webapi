#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="${1:-/app}"

"${SCRIPT_DIR}/deploy.sh" "${BASE_DIR}"
