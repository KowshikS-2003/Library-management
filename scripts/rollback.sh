#!/usr/bin/env bash
# rollback.sh - Roll the running container back to the previous image
#               (or to a specific tag).
# Usage:
#   ./rollback.sh <image_ref>                # roll back to :previous
#   ./rollback.sh <image_ref> <tag>          # roll back to a specific tag
set -euo pipefail

IMAGE_REF="${1:?image_ref required, e.g. ghcr.io/owner/library-management}"
TARGET_TAG="${2:-previous}"
CONTAINER_NAME="${CONTAINER_NAME:-library-management}"
APP_PORT="${APP_PORT:-8080}"

if ! docker image inspect "${IMAGE_REF}:${TARGET_TAG}" >/dev/null 2>&1; then
  echo "Image ${IMAGE_REF}:${TARGET_TAG} not present locally - pulling..."
  docker pull "${IMAGE_REF}:${TARGET_TAG}"
fi

# Promote target back to :latest so future runs also use it until next deploy
docker tag "${IMAGE_REF}:${TARGET_TAG}" "${IMAGE_REF}:latest"

docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p "${APP_PORT}:8080" \
  -e SPRING_PROFILES_ACTIVE=staging \
  "${IMAGE_REF}:${TARGET_TAG}"

echo "Rolled back to ${IMAGE_REF}:${TARGET_TAG}"
docker ps --filter "name=${CONTAINER_NAME}"
