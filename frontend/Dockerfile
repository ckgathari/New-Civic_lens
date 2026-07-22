# ---- Build Stage ----
FROM node:20-alpine AS builder

WORKDIR /build

# Cache npm dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source
COPY . .

# Bake the backend API URL into the bundle at build time.
# Override via --build-arg VITE_API_URL=... if needed.
ARG VITE_API_URL=https://civiclens-backend-aynk.onrender.com/api
ENV VITE_API_URL=${VITE_API_URL}

RUN npm run build

# ---- Runtime Stage ----
FROM nginx:1.25-alpine

# Copy built SPA assets
COPY --from=builder /build/dist /usr/share/nginx/html

# Copy nginx config template — Render injects $PORT at runtime
# The official nginx image processes /etc/nginx/templates/*.template via envsubst
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Render assigns a dynamic $PORT; default to 80 locally
ENV PORT=80
EXPOSE 80

# nginx entrypoint processes the template then starts nginx
CMD ["/docker-entrypoint.sh", "nginx", "-g", "daemon off;"]
