# backend-v2 Docker Usage

## Build

```powershell
powershell -ExecutionPolicy Bypass -File docker-build-optimized.ps1 -Environment prod
docker build -f ../../backend-v2/Dockerfile -t medicine-backend-v2:latest ../..
```

## Validate

```powershell
docker compose -f docker-compose.prod.yml config
powershell -ExecutionPolicy Bypass -File docker-acceptance-check.ps1 -ImageName medicine-backend-v2 -Tag latest
```

## Run

```powershell
docker compose -f docker-compose.dev.yml up -d
docker compose -f docker-compose.prod.yml up -d
```

- Dev frontend: `http://localhost:8080`
- Dev backend health: `http://localhost:18080/health`
- Prod backend health inside container: `http://localhost:8080/health`
