#!/bin/bash

# Script to build and start Civic Lens in Docker

set -e

echo "🚀 Starting Civic Lens containerized deployment..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}❌ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

# Determine environment
ENV=${1:-dev}

if [ "$ENV" = "prod" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
    ENV_FILE=".env.prod"
    echo -e "${BLUE}📦 Using PRODUCTION configuration${NC}"
else
    COMPOSE_FILE="docker-compose.yml"
    ENV_FILE=".env.dev"
    echo -e "${BLUE}📦 Using DEVELOPMENT configuration${NC}"
fi

# Check if environment file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}⚠️  Environment file not found: $ENV_FILE${NC}"
    echo "Creating from .env.example..."
    cp .env.example "$ENV_FILE"
    echo -e "${YELLOW}✏️  Please update $ENV_FILE with your settings${NC}"
fi

# Build images
echo -e "${BLUE}🔨 Building Docker images...${NC}"
docker-compose -f "$COMPOSE_FILE" build

# Start services
echo -e "${BLUE}🚀 Starting services...${NC}"
docker-compose -f "$COMPOSE_FILE" up -d

# Wait for services to be healthy
echo -e "${BLUE}⏳ Waiting for services to be healthy...${NC}"
sleep 10

# Check health status
BACKEND_HEALTH=$(docker-compose -f "$COMPOSE_FILE" ps backend | grep -q "healthy" && echo "✅" || echo "⚠️")
FRONTEND_HEALTH=$(docker-compose -f "$COMPOSE_FILE" ps frontend | grep -q "healthy" && echo "✅" || echo "⚠️")
DB_HEALTH=$(docker-compose -f "$COMPOSE_FILE" ps postgres | grep -q "healthy" && echo "✅" || echo "⚠️")

echo ""
echo -e "${GREEN}✅ Deployment Status:${NC}"
echo -e "  Backend:  $BACKEND_HEALTH"
echo -e "  Frontend: $FRONTEND_HEALTH"
echo -e "  Database: $DB_HEALTH"
echo ""

# Display access URLs
if [ "$ENV" = "prod" ]; then
    echo -e "${GREEN}🌐 Access your application:${NC}"
    echo -e "  Frontend:  https://your-domain"
    echo -e "  Backend:   https://your-domain/api"
    echo -e "  Database:  Managed via Docker (port 5432)"
else
    echo -e "${GREEN}🌐 Access your application:${NC}"
    echo -e "  Frontend:  http://localhost:3000"
    echo -e "  Backend:   http://localhost:8080"
    echo -e "  Database:  localhost:5432"
fi

echo ""
echo -e "${GREEN}📋 Useful commands:${NC}"
echo -e "  View logs:       ${BLUE}docker-compose -f $COMPOSE_FILE logs -f${NC}"
echo -e "  Stop services:   ${BLUE}docker-compose -f $COMPOSE_FILE down${NC}"
echo -e "  Stop & cleanup:  ${BLUE}docker-compose -f $COMPOSE_FILE down -v${NC}"
echo -e "  Backend logs:    ${BLUE}docker-compose -f $COMPOSE_FILE logs -f backend${NC}"
echo -e "  Frontend logs:   ${BLUE}docker-compose -f $COMPOSE_FILE logs -f frontend${NC}"
echo -e "  DB logs:         ${BLUE}docker-compose -f $COMPOSE_FILE logs -f postgres${NC}"
echo ""
