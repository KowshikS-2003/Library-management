#!/usr/bin/env bash
# deploy.sh - Manual deployment helper (mirrors what cd.yml does on the host).
# Usage:  ./deploy.sh <image_ref> <tag>
# Example: ./deploy.sh ghcr.io/owner/library-management 1a2b3c4
set -euo pipefail

IMAGE_REF="${1:?image_ref required, e.g. ghcr.io/owner/library-management}"
IMAGE_TAG="${2:?image tag required, e.g. short-sha}"
CONTAINER_NAME="${CONTAINER_NAME:-library-management}"
APP_PORT="${APP_PORT:-8080}"

# 1. Save current :latest as :previous (rollback target)
if docker image inspect "${IMAGE_REF}:latest" >/dev/null 2>&1; then
  docker tag "${IMAGE_REF}:latest" "${IMAGE_REF}:previous"
  echo "Old :latest tagged as :previous"
fi

# 2. Pull new image and promote to :latest
docker pull "${IMAGE_REF}:${IMAGE_TAG}"
docker tag  "${IMAGE_REF}:${IMAGE_TAG}" "${IMAGE_REF}:latest"

# 3. Replace running container
docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p "${APP_PORT}:8080" \
  -e SPRING_PROFILES_ACTIVE=staging \
  "${IMAGE_REF}:latest"

echo "Deployed ${IMAGE_REF}:${IMAGE_TAG}. Container: ${CONTAINER_NAME} on :${APP_PORT}"
