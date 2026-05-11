# Basic Cloud Deployment Flow

**Project:** Library Management (Spring Boot 3 / Java 17)
**Artifact:** `library-management.jar` packaged as a Docker image
**Scope:** Conceptual cloud deployment flow + a working local demo using the
scripts in [scripts/deploy.sh](scripts/deploy.sh) and [scripts/rollback.sh](scripts/rollback.sh).

---

## 1. Overview

The deployment pipeline has five logical stages:

| # | Stage              | Tool / Command                                  | Where it runs                |
|---|--------------------|-------------------------------------------------|------------------------------|
| 1 | Build image        | `docker build` (multi-stage [Dockerfile](Dockerfile)) | CI runner / local dev        |
| 2 | Push to registry   | `docker push` to GHCR / ACR / ECR / Docker Hub  | CI runner                    |
| 3 | Deploy container   | `docker run` (or `kubectl apply`)               | Target host / cluster        |
| 4 | Update version     | Pull new tag, retag `:latest`, restart          | Target host                  |
| 5 | Rollback           | Re-deploy `:previous` tag                       | Target host                  |

```
 ┌────────┐   build    ┌──────────┐   push    ┌────────────┐   pull/run   ┌────────────┐
 │ Source │ ─────────► │  Image   │ ────────► │  Registry  │ ───────────► │   Server   │
 └────────┘            └──────────┘           └────────────┘              └────────────┘
                                                                          run / update / rollback
```

---

## 2. Build the Image

The project ships a multi-stage [Dockerfile](Dockerfile) that:

1. Builds the JAR with Maven inside a `maven:3.9-eclipse-temurin-17` stage.
2. Extracts Spring Boot layers (`-Djarmode=layertools`) for better layer caching.
3. Copies layers into a slim `eclipse-temurin:17-jre-alpine` runtime stage.
4. Runs as a non-root user `app` on port `8080`.

**Build locally:**

```bash
# Tag using the Git short SHA so every build is uniquely addressable
GIT_SHA=$(git rev-parse --short HEAD)
docker build -t ghcr.io/<owner>/library-management:${GIT_SHA} .
docker tag   ghcr.io/<owner>/library-management:${GIT_SHA} \
             ghcr.io/<owner>/library-management:latest
```

Two tags are produced on every build:

- `:<git-sha>` — immutable, points to one exact build (good for rollbacks).
- `:latest`   — mutable pointer to the most recently deployed build.

---

## 3. Push to Registry (Conceptual)

In a real environment the CI job authenticates to a container registry and
pushes both tags:

```bash
echo "$REGISTRY_TOKEN" | docker login ghcr.io -u <owner> --password-stdin
docker push ghcr.io/<owner>/library-management:${GIT_SHA}
docker push ghcr.io/<owner>/library-management:latest
```

Common registries:

| Registry       | Host                                    |
|----------------|-----------------------------------------|
| GitHub (GHCR)  | `ghcr.io/<owner>/<image>`               |
| Azure (ACR)    | `<acr-name>.azurecr.io/<image>`         |
| AWS (ECR)      | `<acct>.dkr.ecr.<region>.amazonaws.com` |
| Docker Hub     | `docker.io/<user>/<image>`              |

For a **local demo** no actual push is required — the local Docker daemon
already has the image, and `deploy.sh` will reuse it.

---

## 4. Deploy the Container

`deploy.sh` is the manual deployment helper. It performs three actions:

1. Tags the currently running `:latest` as `:previous` (rollback safety net).
2. Pulls the requested tag and re-tags it as `:latest`.
3. Removes the old container and starts a new one with the same name/port.

```bash
./scripts/deploy.sh ghcr.io/<owner>/library-management <git-sha>
```

Equivalent to:

```bash
docker tag  ghcr.io/<owner>/library-management:latest \
            ghcr.io/<owner>/library-management:previous
docker pull ghcr.io/<owner>/library-management:<git-sha>
docker tag  ghcr.io/<owner>/library-management:<git-sha> \
            ghcr.io/<owner>/library-management:latest
docker rm -f library-management
docker run  -d --name library-management --restart unless-stopped \
            -p 8080:8080 -e SPRING_PROFILES_ACTIVE=staging \
            ghcr.io/<owner>/library-management:latest
```

Verify:

```bash
docker ps --filter name=library-management
curl -f http://localhost:8080/actuator/health
```

---

## 5. Update the Version

Updating to a new release is just another `deploy.sh` call with a new tag.
Because the previous `:latest` is preserved as `:previous` first, the upgrade
is always reversible.

```bash
# v1 already running; deploy v2
./scripts/deploy.sh ghcr.io/<owner>/library-management v2
```

State after the call:

| Tag                     | Points to |
|-------------------------|-----------|
| `:latest`               | v2        |
| `:previous`             | v1        |
| `:v1`, `:v2` (immutable)| v1, v2    |

---

## 6. Rollback to the Previous Version

`rollback.sh` reverses the last deploy:

```bash
./scripts/rollback.sh ghcr.io/<owner>/library-management
# or roll back to a specific historical tag
./scripts/rollback.sh ghcr.io/<owner>/library-management v1
```

It performs:

1. Ensures the target image is present locally (pulls if missing).
2. Re-tags it as `:latest` so subsequent deploys start from a known good base.
3. Stops/removes the failing container.
4. Starts a new container from the rollback tag.

### "If deployment fails, how do you rollback?"

The simple, repeatable answer used by this project:

1. **Use the previous image tag** — every deploy preserves the prior `:latest`
   as `:previous`, and every build is also tagged with its immutable Git SHA.
2. **Stop the new (broken) container** — `docker rm -f library-management`.
3. **Start the old container** — `docker run -d ... <image>:previous` (this is
   exactly what [scripts/rollback.sh](scripts/rollback.sh) does).
4. **Verify** — `docker ps` and `curl /actuator/health` to confirm the old
   version is healthy again.

Because the image is immutable and identified by tag, rollback is just
"point the runtime at the previous tag and restart" — no rebuild required.

---

## 7. Local End-to-End Demo

```bash
# 0. From project root
IMAGE=ghcr.io/demo/library-management

# 1. Build v1 and "deploy" it
docker build -t ${IMAGE}:v1 .
docker tag   ${IMAGE}:v1 ${IMAGE}:latest
./scripts/deploy.sh ${IMAGE} v1
curl -f http://localhost:8080/actuator/health     # OK

# 2. Build v2 and update
docker build -t ${IMAGE}:v2 .
./scripts/deploy.sh ${IMAGE} v2
curl -f http://localhost:8080/actuator/health     # OK (or fails -> rollback)

# 3. Rollback to v1
./scripts/rollback.sh ${IMAGE}                    # uses :previous
docker ps --filter name=library-management
curl -f http://localhost:8080/actuator/health     # OK again
```

---

## 8. Summary

- **Build** with the multi-stage [Dockerfile](Dockerfile); tag with Git SHA + `:latest`.
- **Push** both tags to a container registry (GHCR/ACR/ECR/Docker Hub).
- **Deploy** with [scripts/deploy.sh](scripts/deploy.sh): preserves `:previous`, pulls new tag, restarts container.
- **Update** = run `deploy.sh` with the new tag; previous version is auto-saved.
- **Rollback** with [scripts/rollback.sh](scripts/rollback.sh): re-tags `:previous` (or any prior tag) as `:latest` and restarts.

Rollback works because **images are immutable** and **tags are cheap** — we
never lose the last good version, so recovery is always one command away.
