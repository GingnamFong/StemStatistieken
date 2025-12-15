# Docker Automated Pipeline

### GitLab CI/CD Variables

- `DOCKER_HUB_PASSWORD`:
- `DOCKER_HUB_USER`:

## Pipeline 

The pipeline has the following steps:

1. **Build**: Compiles the frontend and backend services
2. **Test**: Runs tests for both services
3. **Build Docker**: Builds and pushes Docker images to Docker Hub
4. **Deploy**: Deploys 

## Docker Images

The pipeline automatically builds and pushes the following:

- `gingnam/hva-frontend:latest` 
- `gingnam/hva-backend:latest`

## Local Development

### Using Docker Compose

To run the entire stack locally:

```bash
docker-compose up --build
```

This will:
- Build and run the frontend on port 80
- Build and run the backend on port 8081


## Pipeline Triggers

The Docker build jobs run automatically when:
- Changes are made to `frontend_vue/**/*` or `XMLParser/**/*`
- On branches: `main`


