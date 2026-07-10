# 🐳 Containerization & DigitalOcean Deployment Setup

Your Civic Lens application is now ready for containerization and deployment on DigitalOcean!

## 📋 What's Been Created

### Docker Files
- **backend/Dockerfile** - Multi-stage build for Spring Boot backend (Java 21)
- **frontend/Dockerfile** - Multi-stage build for React frontend (Node 20)
- **backend/.dockerignore** & **frontend/.dockerignore** - Optimized Docker builds
- **docker-compose.yml** - Development environment (PostgreSQL + Backend + Frontend)
- **docker-compose.prod.yml** - Production-ready environment with logging

### Configuration Files
- **.env.example** - Template environment variables
- **.env.dev** - Development environment variables (create from example)
- **.env.prod** - Production environment variables (create from example)

### Deployment Guides & Scripts
- **DIGITALOCEAN_DEPLOYMENT.md** - Complete deployment guide with 3 options
- **deploy.sh** - Quick start script for local Docker Compose
- **build-and-push.sh** - Build and push images to DigitalOcean Container Registry

---

## 🚀 Quick Start (Development)

### 1. Generate Maven Wrapper (if missing)
```bash
cd backend
mvn wrapper:wrapper
cd ..
```

### 2. Start all services with Docker Compose
```bash
chmod +x deploy.sh
./deploy.sh dev
```

Or run manually:
```bash
docker-compose up -d
```

### 3. Access the application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432 (postgres:5432)

### 4. View logs
```bash
docker-compose logs -f backend   # Backend logs
docker-compose logs -f frontend  # Frontend logs
docker-compose logs -f postgres  # Database logs
```

### 5. Stop services
```bash
docker-compose down              # Stop services
docker-compose down -v           # Remove volumes (resets database)
```

---

## 🌐 Deployment to DigitalOcean

### Prerequisites
- DigitalOcean account
- Docker installed locally
- `doctl` CLI installed: https://docs.digitalocean.com/reference/doctl/

### Option 1: App Platform (Recommended for Development)

**Easiest option - no Kubernetes knowledge needed!**

```bash
# 1. Authenticate with DigitalOcean
doctl auth init

# 2. Create container registry
doctl registry create civic-lens

# 3. Login and push images
doctl registry login
chmod +x build-and-push.sh
./build-and-push.sh registry.digitalocean.com civic-lens latest

# 4. Create app in DigitalOcean Console
# - Go to App Platform
# - Create from container registry
# - Add 3 services: backend, frontend, and connect to managed DB
```

### Option 2: DigitalOcean Kubernetes

**Best for production scaling**

```bash
# 1. Create Kubernetes cluster
doctl kubernetes cluster create civic-lens-prod --size s-2vcpu-4gb --num-nodes 3

# 2. Deploy to cluster
kubectl apply -f kubernetes/

# 3. Monitor deployment
kubectl get pods -n civic-lens
```

### Option 3: DigitalOcean Droplet

**Most control, requires manual setup**

```bash
# 1. Create droplet via console
# 2. SSH into droplet
# 3. Install Docker & Docker Compose
# 4. Clone repo and run production compose file
```

See **DIGITALOCEAN_DEPLOYMENT.md** for detailed instructions.

---

## 🔧 Environment Configuration

### Development (.env.dev)
```bash
# Copy from .env.example and customize for development
SPRING_PROFILES_ACTIVE=dev
VITE_API_BASE_URL=http://localhost:8080
```

### Production (.env.prod)
```bash
# Create with production values
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://your-managed-db:25060/civic_lens_prod
JWT_SECRET=your-long-random-secret-key-minimum-32-characters
CORS_ALLOWED_ORIGINS=https://your-domain.com
VITE_API_BASE_URL=https://your-api-domain.com
```

**⚠️ Never commit .env files with secrets to version control!**

---

## 📊 Health Checks

Each service includes health checks:
- **Backend**: HTTP health endpoint `/actuator/health`
- **Frontend**: HTTP GET to service port
- **Database**: PostgreSQL connectivity check

View health status:
```bash
docker-compose ps
```

---

## 🔐 Security Checklist

- [ ] Change default JWT secret in production
- [ ] Use managed PostgreSQL instead of self-hosted for production
- [ ] Enable HTTPS/SSL on your domain
- [ ] Set proper CORS origins
- [ ] Use strong database password
- [ ] Store secrets in DigitalOcean App Platform or environment variables
- [ ] Enable database backups
- [ ] Configure firewall rules
- [ ] Keep Docker images updated

---

## 📈 Next Steps

1. **Test locally**: Run `./deploy.sh dev` and verify everything works
2. **Update environment**: Customize `.env.prod` for your production setup
3. **Generate Maven wrapper**: Run `mvn wrapper:wrapper` in backend if not present
4. **Push to DigitalOcean**: Follow DIGITALOCEAN_DEPLOYMENT.md
5. **Monitor**: Set up logging and monitoring in DigitalOcean dashboard
6. **Scale**: Use load balancing and auto-scaling features as needed

---

## 🆘 Troubleshooting

### Services not starting?
```bash
# Check logs
docker-compose logs

# Restart services
docker-compose restart

# Full reset
docker-compose down -v
docker-compose up -d
```

### Database connection issues?
```bash
# Check database health
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Access database directly
docker exec -it civic_lens_postgres_dev psql -U civic_user -d civic_lens_prod
```

### Frontend can't reach backend?
- Check `CORS_ALLOWED_ORIGINS` in backend config
- Ensure `VITE_API_BASE_URL` points to correct backend URL
- Check network connectivity between containers

### Out of disk space?
```bash
# Clean up Docker resources
docker system prune -a
docker volume prune
```

---

## 📚 Useful Resources

- Docker Documentation: https://docs.docker.com/
- DigitalOcean App Platform: https://docs.digitalocean.com/products/app-platform/
- DigitalOcean Kubernetes: https://docs.digitalocean.com/products/kubernetes/
- Spring Boot Deployment: https://spring.io/guides/topicals/spring-boot-docker/
- React Docker: https://react.dev/learn/deployment

---

## 💡 Tips

- Use `.dockerignore` to exclude unnecessary files from builds
- Multi-stage Dockerfiles reduce final image size
- Health checks help with automatic recovery
- Docker Compose environment variables make it easy to switch configurations
- Always test in development before deploying to production

Happy deploying! 🚀
