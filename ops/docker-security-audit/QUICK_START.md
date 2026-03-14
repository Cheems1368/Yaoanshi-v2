# backend-v2 Docker Quick Start

## Development

```powershell
powershell -ExecutionPolicy Bypass -File docker-build-optimized.ps1 -Environment dev
docker compose -f docker-compose.dev.yml up -d
```

- Frontend: `http://localhost:8080`
- Backend health: `http://localhost:18080/health`

## Production

```powershell
docker compose -f docker-compose.prod.yml config
powershell -ExecutionPolicy Bypass -File docker-build-optimized.ps1 -Environment prod
docker compose -f docker-compose.prod.yml up -d
```

- Pharmacist static assets must exist under `../../frontend/pharmacist/dist`
- Secret templates live in `secrets/*.example`
- Replace placeholder env vars before real production startup, otherwise `backend-v2` will fail fast under `prod`
