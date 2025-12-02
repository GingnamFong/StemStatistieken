# Deployment, Docker image pushen op Docker hub

# Backend (XMLParser)
cd XMLParser
docker build -t kevinkoudhoofd/hva-backend:v(versie) .
docker push kevinkoudhoofd/hva-backend:v(versie)

# Frontend 
cd ../frontend_vue
docker build -t kevinkoudhoofd/hva-frontend:v(versie) .
docker push kevinkoudhoofd/hva-frontend:v(versie)