# Phase 4 Local Trial-Run Handbook

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| Node.js | 20+ | `node -v` |
| Java | 17.0.12+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Docker Desktop | latest | Legacy MySQL/Redis/server |
| Playwright | 1.58+ | E2E browser tests |

## Service Map

| Service | Port | Start Command |
|---------|------|---------------|
| Legacy MySQL | 13306 | Docker Compose |
| Legacy Redis | 16379 | Docker Compose |
| Legacy Server | 48080 | Docker Compose |
| Local MySQL | 3306 | Native or Docker |
| backend-v2 | 8080 | Maven spring-boot:run |
| Patient App | 3001 | `npm run dev` |
| Doctor Web | 3002 | `npm run dev` |
| Pharmacist Web | 3003 | `npm run dev` |

## Step-by-step Launch

### 1. Start legacy Docker stack

```powershell
docker compose -f D:/yaoanshi/backend/medicinal-his-delivery/docker-compose.yml up -d mysql redis server
```

Verify: ports 13306, 16379, 48080 are listening.

### 2. Start backend-v2

```powershell
powershell -ExecutionPolicy Bypass -File D:/yaoanshi/backend-v2/scripts/run-local-with-env.ps1
```

Or manually:

```powershell
$env:JAVA_HOME = "D:\Java\Java 17"
$env:SPRING_PROFILES_ACTIVE = "local"
mvn -f D:/yaoanshi/backend-v2/pom.xml spring-boot:run
```

Verify: `curl http://127.0.0.1:8080/health` returns `{"code":200}`.

### 3. Start three frontends

```powershell
# Terminal 1: Patient app
cd D:/yaoanshi/frontend/huanzhe && npm run dev

# Terminal 2: Doctor web
cd D:/yaoanshi/frontend/yisheng/web-admin && npm run dev

# Terminal 3: Pharmacist web
cd D:/yaoanshi/frontend/pharmacist && npm run dev
```

### 4. Run automated smoke test

```powershell
powershell -ExecutionPolicy Bypass -File D:/yaoanshi/ops/scripts/phase4-local-run.ps1 -SkipBuild
```

### 5. Run E2E browser tests individually

```powershell
cd D:/yaoanshi/frontend/huanzhe
npm run e2e:auth-bind
npm run e2e:prescription-review
npm run e2e:medication-family
npm run e2e:helporder-collab
npm run e2e:admin-notifications
```

## Shutdown

```powershell
# Stop backend-v2: Ctrl+C in the Maven terminal
# Stop frontends: Ctrl+C in each Vite terminal
docker compose -f D:/yaoanshi/backend/medicinal-his-delivery/docker-compose.yml down
```

## Known Issues

1. Object storage uses stub — prescription attachments return placeholder URLs
2. OCR always returns MANUAL_REVIEW_REQUIRED — pharmacist must manually enter items
3. AI assistant depends on SiliconFlow account balance
4. SEC-004 default secrets must be overridden in production via environment variables
