# Bouw fase
FROM node:22-alpine AS build
# Werk in de app map
WORKDIR /app

# kopiëer alle bestanden die beginnen met package en eindigen met json
COPY package*.json ./
# installeer dependencies zonder meldingen
RUN npm ci --no-audit --no-fund

# Alle bestanden kopiëren en in de app map bouwen   
COPY . .
# Bouw de app
RUN npm run build


# Run fase
FROM nginx:alpine

# Statische build naar de Nginx webroot
COPY --from=build /app/dist /usr/share/nginx/html

# Eigen Nginx-config met SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

# poort 80 alleen toegankelijk maken
EXPOSE 80

# zorg ervoor dat nginx op de voorkant runned niet achtergrond
CMD ["nginx", "-g", "daemon off;"]


