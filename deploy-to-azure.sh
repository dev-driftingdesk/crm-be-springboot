#!/bin/bash

# Azure Container Apps Deployment Script for CeedPods Service
# Make sure you're logged in to Azure CLI: az login

set -e

# Configuration variables

RESOURCE_GROUP="keycloak-rg"
CONTAINER_APP_NAME="ceedpodservice"
CONTAINER_APP_ENV="ceedpods"
ACR_NAME="ceedpodsregister"
IMAGE_NAME="ceedpodservice"
IMAGE_TAG="v1.1.3"

echo "🚀 Starting deployment of CeedPods Service to Azure Container Apps..."

# Get ACR login server
ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
FULL_IMAGE_NAME="$ACR_LOGIN_SERVER/$IMAGE_NAME:$IMAGE_TAG"

echo "📦 Using image: $FULL_IMAGE_NAME"

# Get ACR credentials
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value --output tsv)

# Check if container app already exists
APP_EXISTS=$(az containerapp show --name $CONTAINER_APP_NAME --resource-group $RESOURCE_GROUP --query "properties.provisioningState" --output tsv 2>/dev/null || echo "NOT_FOUND")

if [ "$APP_EXISTS" = "NOT_FOUND" ]; then
  echo "🏗️  Creating new container app: $CONTAINER_APP_NAME"
  
  az containerapp create \
    --name $CONTAINER_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --environment $CONTAINER_APP_ENV \
    --image $FULL_IMAGE_NAME \
    --registry-server $ACR_LOGIN_SERVER \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --cpu 1.0 \
    --memory 2.0Gi \
    --min-replicas 1 \
    --max-replicas 3 \
    --ingress external \
    --target-port 8084 \
    --env-vars \
      "SPRING_PROFILES_ACTIVE=prod" \
      "server.port=8084" \
      "server.address=0.0.0.0" \
      "APP_URL=https://api.teamhandle.com" \
      "MONGODB_URI=secretref:mongodb-uri" \
      "KEYCLOAK_SERVER_URL=https://ceedpods-keycloak.wittycliff-5b88c7b4.westus2.azurecontainerapps.io" \
      "KEYCLOAK_REALM_NAME=ceedpods" \
      "KEYCLOAK_CLIENT_ID=ceedpods" \
      "KEYCLOAK_CLIENT_SECRET=secretref:keycloak-client-secret" \
      "KEYCLOAK_ADMIN_USERNAME=secretref:keycloak-admin-username" \
      "KEYCLOAK_ADMIN_PASSWORD=secretref:keycloak-admin-password" \
    --secrets \
      "mongodb-uri=mongodb+srv://amithrathnayaka34_db_user:ceedpods1997@ceedpods.dszv2xr.mongodb.net/ceedpods?retryWrites=true&w=majority&appName=ceedpods&ssl=true&tlsAllowInvalidCertificates=true&tlsAllowInvalidHostnames=true" \
      "keycloak-client-secret=TVdDpeJHPl2uegRlIhlXL7TCuVdYoirR" \
      "keycloak-admin-username=admin" \
      "keycloak-admin-password=admin"

  echo "✅ Container app created successfully!"
else
  echo "🔄 Updating existing container app: $CONTAINER_APP_NAME"
  
  # First, update secrets using separate commands
  echo "🔐 Updating secrets..."
  
  az containerapp secret set \
    --name $CONTAINER_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --secrets \
      "mongodb-uri=mongodb+srv://amithrathnayaka34_db_user:ceedpods1997@ceedpods.dszv2xr.mongodb.net/ceedpods?retryWrites=true&w=majority&appName=ceedpods&ssl=true&tlsAllowInvalidCertificates=true&tlsAllowInvalidHostnames=true" \
      "keycloak-client-secret=TVdDpeJHPl2uegRlIhlXL7TCuVdYoirR" \
      "keycloak-admin-username=admin" \
      "keycloak-admin-password=admin"
  
  # Then update the container app with new image and configuration
  echo "🔄 Updating container configuration..."
  
  az containerapp update \
    --name $CONTAINER_APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --image $FULL_IMAGE_NAME \
    --cpu 1.0 \
    --memory 2.0Gi \
    --min-replicas 1 \
    --max-replicas 3 \
    --set-env-vars \
      "SPRING_PROFILES_ACTIVE=prod" \
      "server.port=8084" \
      "server.address=0.0.0.0" \
      "APP_URL=https://api.teamhandle.com" \
      "MONGODB_URI=secretref:mongodb-uri" \
      "KEYCLOAK_SERVER_URL=https://ceedpods-keycloak.wittycliff-5b88c7b4.westus2.azurecontainerapps.io" \
      "KEYCLOAK_REALM_NAME=ceedpods" \
      "KEYCLOAK_CLIENT_ID=ceedpods" \
      "KEYCLOAK_CLIENT_SECRET=secretref:keycloak-client-secret" \
      "KEYCLOAK_ADMIN_USERNAME=secretref:keycloak-admin-username" \
      "KEYCLOAK_ADMIN_PASSWORD=secretref:keycloak-admin-password"

  echo "✅ Container app updated successfully!"
fi

# Get the FQDN
FQDN=$(az containerapp show \
  --name $CONTAINER_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn \
  --output tsv)

echo "🌐 Application URL: https://$FQDN"
echo "🔗 API Base URL: https://$FQDN/api/v1"
echo "❤️  Health Check: https://$FQDN/api/v1/actuator/health"

echo "🎉 Deployment completed successfully!"
echo ""
echo "📝 Next steps:"
echo "1. Test the health endpoint: curl https://$FQDN/api/v1/actuator/health"
echo "2. Check logs: az containerapp logs show --name $CONTAINER_APP_NAME --resource-group $RESOURCE_GROUP"
echo "3. Update your frontend to use: https://$FQDN/api/v1"