#!/bin/bash

# Build Docker images for DigitalOcean deployment

set -e

REGISTRY=${1:-registry.digitalocean.com}
NAMESPACE=${2:-your-registry-name}
TAG=${3:-latest}

echo "🔨 Building and pushing Docker images to DigitalOcean Container Registry"
echo "Registry: $REGISTRY/$NAMESPACE"
echo "Tag: $TAG"
echo ""

# Build and push backend
echo "📦 Building backend image..."
cd backend
docker build -t $REGISTRY/$NAMESPACE/civic-lens-backend:$TAG .
echo "📤 Pushing backend image..."
docker push $REGISTRY/$NAMESPACE/civic-lens-backend:$TAG
cd ..

# Build and push frontend
echo "📦 Building frontend image..."
cd frontend
docker build -t $REGISTRY/$NAMESPACE/civic-lens-frontend:$TAG .
echo "📤 Pushing frontend image..."
docker push $REGISTRY/$NAMESPACE/civic-lens-frontend:$TAG
cd ..

echo ""
echo "✅ Images pushed successfully!"
echo ""
echo "Use these image URLs in DigitalOcean App Platform:"
echo "  Backend:  $REGISTRY/$NAMESPACE/civic-lens-backend:$TAG"
echo "  Frontend: $REGISTRY/$NAMESPACE/civic-lens-frontend:$TAG"
