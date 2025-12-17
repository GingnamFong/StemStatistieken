# Build stage
FROM node:22-alpine AS build
# Set working directory
WORKDIR /app

# Runtime detection automatically uses the correct backend URL

# Copy package files
COPY package*.json ./
# Install dependencies without notifications
RUN npm ci --no-audit --no-fund

# Copy all files and build in app directory
COPY . .

# Build the app
RUN npm run build


# Runtime stage
FROM nginx:alpine

# Copy static build to Nginx webroot
COPY --from=build /app/dist /usr/share/nginx/html

# Custom Nginx config with SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Run nginx in foreground, not as daemon
CMD ["nginx", "-g", "daemon off;"]


