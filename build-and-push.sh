#!/bin/bash

# Build and Push Script for CeedPods Service
# This script builds the Spring Boot project and pushes Docker image to Azure Container Registry

set -e

# Configuration variables
RESOURCE_GROUP="keycloak-rg"
ACR_NAME="ceedpodsregister"
IMAGE_NAME="ceedpodservice"
IMAGE_TAG="${1:-v1.1.3}"

echo "🚀 Building and pushing CeedPods Service..."
echo "📦 Image: $ACR_NAME.azurecr.io/$IMAGE_NAME:$IMAGE_TAG"

# Step 1: Create ACR if it doesn't exist
echo "🔍 Checking if ACR exists..."
if ! az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP >/dev/null 2>&1; then
    echo "📦 Creating Azure Container Registry: $ACR_NAME"
    az acr create \
        --resource-group $RESOURCE_GROUP \
        --name $ACR_NAME \
        --sku Basic \
        --location westus2
else
    echo "✅ ACR $ACR_NAME already exists"
fi

# Step 2: Login to ACR
echo "🔐 Logging into Azure Container Registry..."
az acr login --name $ACR_NAME

# Step 3: Get ACR login server
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/$IMAGE_NAME:$IMAGE_TAG"

# Step 4: Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean

# Step 5: Build the Spring Boot application
echo "🔨 Building Spring Boot application..."
mvn package -DskipTests

# Step 6: Build Docker image for Linux/AMD64 platform
echo "🐳 Building Docker image for Linux/AMD64: $FULL_IMAGE_NAME"
docker build --platform linux/amd64 -t $IMAGE_NAME:$IMAGE_TAG .
docker build --platform linux/amd64 -t $IMAGE_NAME:latest .

# Step 7: Tag image for ACR
echo "🏷️  Tagging image for ACR..."
docker tag $IMAGE_NAME:$IMAGE_TAG $FULL_IMAGE_NAME
docker tag $IMAGE_NAME:latest $ACR_LOGIN_SERVER/$IMAGE_NAME:latest

# Step 8: Push to ACR
echo "📤 Pushing image to Azure Container Registry..."
docker push $FULL_IMAGE_NAME
docker push $ACR_LOGIN_SERVER/$IMAGE_NAME:latest

# Step 9: Clean up local images (optional)
echo "🧹 Cleaning up local images..."
docker rmi $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest || true

echo ""
echo "✅ Build and push completed successfully!"
echo "📦 Image: $FULL_IMAGE_NAME"
echo "🌐 Registry: $ACR_LOGIN_SERVER"
echo ""
echo "To deploy this image, run:"
echo "  ./deploy-to-azure.sh $IMAGE_TAG"
echo ""