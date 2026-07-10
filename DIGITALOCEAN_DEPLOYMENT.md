# Civic Lens - DigitalOcean Deployment Guide

## Prerequisites
- DigitalOcean account
- Docker installed locally (for building and testing)
- Docker CLI or DigitalOcean Container Registry access
- `doctl` CLI installed (optional, for automated management)

## Deployment Options

### Option 1: DigitalOcean App Platform (Recommended for Development)
App Platform is the easiest option for development - managed container hosting without managing Kubernetes.

#### Steps:
1. **Push images to DigitalOcean Container Registry:**
   ```bash
   # Authenticate with your registry
   doctl registry login
   
   # Build backend image
   cd backend
   docker build -t registry.digitalocean.com/your-registry/civic-lens-backend:latest .
   docker push registry.digitalocean.com/your-registry/civic-lens-backend:latest
   
   # Build frontend image
   cd ../frontend
   docker build -t registry.digitalocean.com/your-registry/civic-lens-frontend:latest .
   docker push registry.digitalocean.com/your-registry/civic-lens-frontend:latest
   ```

2. **Create managed PostgreSQL database:**
   - Go to Databases in DigitalOcean console
   - Create PostgreSQL 16 cluster
   - Note the connection details

3. **Create App Platform app:**
   - Go to App Platform
   - Create new app from source (GitHub/GitLab repo or container registry)
   - Configure three components:
     - **Backend service**: Use backend image, port 8080
     - **Frontend service**: Use frontend image, port 3000
     - **Database**: Connect to managed PostgreSQL

4. **Set environment variables:**
   For backend service:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:25060/civic_lens_prod`
   - `SPRING_DATASOURCE_USERNAME=doadmin`
   - `SPRING_DATASOURCE_PASSWORD=your-password`
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JWT_SECRET=your-strong-secret-key-here`
   - `CORS_ALLOWED_ORIGINS=https://your-frontend-domain`

   For frontend service:
   - `VITE_API_BASE_URL=https://your-backend-domain`

---

### Option 2: DigitalOcean Kubernetes (for Production)

#### Steps:
1. **Create a Kubernetes cluster:**
   ```bash
   doctl kubernetes cluster create civic-lens-prod --size s-2vcpu-4gb --num-nodes 3
   ```

2. **Get kubeconfig:**
   ```bash
   doctl kubernetes cluster kubeconfig save civic-lens-prod
   ```

3. **Install Kubernetes manifests:**
   ```bash
   # Create namespace
   kubectl create namespace civic-lens
   
   # Create ConfigMaps and Secrets
   kubectl create configmap backend-config --from-literal=SPRING_PROFILES_ACTIVE=prod -n civic-lens
   kubectl create secret generic db-secret --from-literal=username=doadmin --from-literal=password=your-password -n civic-lens
   ```

4. **Deploy services:**
   ```bash
   kubectl apply -f kubernetes/ -n civic-lens
   ```

---

### Option 3: DigitalOcean Droplets (Manual Setup)

#### Steps:
1. **Create Droplet:**
   - Choose Ubuntu 22.04 LTS
   - 2GB RAM minimum (4GB recommended for production)
   - Add SSH key for secure access

2. **SSH into Droplet and install Docker:**
   ```bash
   ssh root@your-droplet-ip
   
   curl -fsSL https://get.docker.com -o get-docker.sh
   sh get-docker.sh
   ```

3. **Clone repository:**
   ```bash
   git clone <your-repo-url>
   cd Civic-Lens
   ```

4. **Update docker-compose for production:**
   - See `docker-compose.prod.yml` in repo

5. **Start services:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

6. **Set up reverse proxy (Nginx):**
   - Install Nginx, configure SSL with Let's Encrypt
   - Forward requests to backend (port 8080) and frontend (port 3000)

---

## Development Workflow

### Local Testing with Docker Compose:
```bash
# Build and start all services
docker-compose up -d

# Check logs
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Stop services
docker-compose down

# Remove volumes (resets database)
docker-compose down -v
```

### Database Migrations:
- Migrations run automatically via Flyway on backend startup
- In development, Flyway is disabled by default - enable in docker-compose if needed

---

## Environment Configuration

### Development (.env.dev)
```
API_URL=http://localhost:8080
DATABASE_HOST=localhost
DATABASE_PORT=5432
JWT_SECRET=dev-key-not-for-production
```

### Production (.env.prod)
```
API_URL=https://your-api-domain
DATABASE_HOST=your-managed-db-host
DATABASE_PORT=25060
JWT_SECRET=your-long-strong-random-secret-key
```

---

## Security Considerations

1. **Never commit `.env` files with secrets** - use DigitalOcean App Platform secrets instead
2. **Use managed PostgreSQL** instead of self-hosted for production
3. **Enable backups** for your database
4. **Use HTTPS** with SSL certificates (Let's Encrypt via Nginx or App Platform)
5. **Restrict firewall rules** to only necessary ports
6. **Rotate JWT secrets** regularly
7. **Keep Docker images updated** with latest security patches

---

## Monitoring & Logs

- **App Platform**: Built-in monitoring and logs in console
- **Kubernetes**: Use `kubectl logs`, Prometheus, or third-party monitoring
- **Droplet**: Use `docker logs`, set up ELK stack, or use DigitalOcean monitoring

---

## Cost Estimation

- **App Platform**: $12/month per service + database costs (~$15/month for shared DB)
- **Kubernetes**: $12/month per node + storage + database
- **Droplet**: $4-6/month for small instance + database

**Recommended for development**: App Platform (easiest to manage, automatic scaling)
**Recommended for production**: Kubernetes (better scaling, self-healing)
