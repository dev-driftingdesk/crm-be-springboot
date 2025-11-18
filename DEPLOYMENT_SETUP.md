# GitHub Actions CI/CD Setup Guide

## Overview

This guide helps you set up automated CI/CD pipeline for the CeedPods Service using GitHub Actions. The pipeline will:

- ✅ Build and test on every push/PR to `dev` branch
- ✅ Create semantic versions automatically
- ✅ Build Docker images and push to Azure Container Registry
- ✅ Deploy to Azure Container Apps on `dev` branch merges
- ✅ Perform health checks after deployment
- ✅ Create GitHub releases with deployment information

## Prerequisites

1. **Azure CLI** installed and logged in
2. **Azure Container Registry** (`ceedpodsregister`) already created
3. **Azure Container Apps Environment** (`ceedpods`) already created
4. **GitHub repository** with admin access

## Required GitHub Secrets

Go to your GitHub repository → Settings → Secrets and Variables → Actions, and add these secrets:

### 🔐 Azure Container Registry Secrets

```bash
# Get ACR credentials
az acr credential show --name ceedpodsregister --resource-group keycloak-rg
```

- **`REGISTRY_USERNAME`**: Your ACR username
- **`REGISTRY_PASSWORD`**: Your ACR password (use password, not password2)

### 🔑 Azure Service Principal Secrets

Create a service principal for GitHub Actions:

```bash
# Create service principal
az ad sp create-for-rbac \
  --name "github-actions-ceedpods" \
  --role contributor \
  --scopes /subscriptions/{SUBSCRIPTION_ID}/resourceGroups/keycloak-rg \
  --sdk-auth
```

- **`AZURE_CREDENTIALS`**: Complete JSON output from the above command

### 🌐 Application Configuration Secrets

Based on your current `deploy-to-azure.sh`, add these secrets:

- **`APP_URL`**: `https://api.teamhandle.com`
- **`KEYCLOAK_SERVER_URL`**: `https://ceedpods-keycloak.wittycliff-5b88c7b4.westus2.azurecontainerapps.io`
- **`KEYCLOAK_REALM_NAME`**: `ceedpods`
- **`KEYCLOAK_CLIENT_ID`**: `ceedpods`

### 🔒 Sensitive Configuration Secrets

⚠️ **SECURITY NOTE**: These secrets are currently in your deployment script. Move them to GitHub secrets:

- **`MONGODB_URI`**: `mongodb+srv://amithrathnayaka34_db_user:ceedpods1997@ceedpods.dszv2xr.mongodb.net/ceedpods?retryWrites=true&w=majority&appName=ceedpods&ssl=true&tlsAllowInvalidCertificates=true&tlsAllowInvalidHostnames=true`
- **`KEYCLOAK_CLIENT_SECRET`**: `TVdDpeJHPl2uegRlIhlXL7TCuVdYoirR`
- **`KEYCLOAK_ADMIN_USERNAME`**: `admin`
- **`KEYCLOAK_ADMIN_PASSWORD`**: `admin`

## How to Add Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and Variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with the exact name and value listed above

## Setting Up Azure Service Principal

### Step 1: Get your subscription ID
```bash
az account show --query id --output tsv
```

### Step 2: Create the service principal
```bash
# Replace {SUBSCRIPTION_ID} with your actual subscription ID
az ad sp create-for-rbac \
  --name "github-actions-ceedpods" \
  --role contributor \
  --scopes /subscriptions/{SUBSCRIPTION_ID}/resourceGroups/keycloak-rg \
  --sdk-auth
```

### Step 3: Save the output as AZURE_CREDENTIALS secret
The output should look like this:
```json
{
  "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "clientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "subscriptionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "tenantId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

Copy this entire JSON and paste it as the value for the `AZURE_CREDENTIALS` secret.

## Versioning Strategy

The pipeline automatically generates versions based on your current `pom.xml` version:

### For Dev Branch (Auto-deployment)
- Format: `{pom-version}-dev.{build-number}.{short-sha}`
- Example: `1.1.0-dev.123.abc1234`
- Image Tag: `v1.1.0-dev-123`

### For Pull Requests (Build only)
- Format: `{pom-version}-pr{pr-number}.{build-number}`
- Example: `1.1.0-pr15.123`
- Image Tag: `pr-15-123`

## Environment Protection

The deployment job uses GitHub Environment protection:

1. Go to **Settings** → **Environments**
2. Create an environment named `production`
3. Add protection rules (optional):
   - Required reviewers
   - Wait timer
   - Branch restrictions

## Testing the Pipeline

### 1. Create a Pull Request
```bash
git checkout -b feature/test-pipeline
echo "# Test change" >> README.md
git add README.md
git commit -m "test: pipeline setup"
git push origin feature/test-pipeline
```
Create a PR to `dev` branch. This will trigger build and test only.

### 2. Merge to Dev Branch
When you merge the PR to `dev`, it will:
1. Build and test the application
2. Create a Docker image with semantic version
3. Push to Azure Container Registry
4. Deploy to Azure Container Apps
5. Run health checks
6. Create a GitHub release

## Monitoring Deployments

### GitHub Actions
- Go to **Actions** tab in your repository
- View workflow runs and logs

### Azure Container Apps
```bash
# Check container app status
az containerapp show \
  --name ceedpodservice \
  --resource-group keycloak-rg \
  --query "properties.provisioningState"

# View logs
az containerapp logs show \
  --name ceedpodservice \
  --resource-group keycloak-rg \
  --follow

# Check health
curl https://{your-app-url}/api/v1/actuator/health
```

## Security Best Practices

1. **Rotate secrets regularly** - Update service principal credentials periodically
2. **Use least privilege** - Service principal only has contributor access to resource group
3. **Monitor deployments** - Review GitHub Actions logs for any issues
4. **Environment protection** - Consider adding required reviewers for production deployments

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify `AZURE_CREDENTIALS` secret is correctly formatted JSON
   - Check service principal has proper permissions

2. **Registry Access Denied**
   - Verify `REGISTRY_USERNAME` and `REGISTRY_PASSWORD` are correct
   - Check ACR admin user is enabled

3. **Health Check Fails**
   - Check application logs in Azure Container Apps
   - Verify all environment variables are set correctly

4. **Deployment Timeout**
   - Increase timeout values in workflow
   - Check Azure resource quotas

### Getting Help

```bash
# View container app logs
az containerapp logs show --name ceedpodservice --resource-group keycloak-rg

# Check container app status
az containerapp show --name ceedpodservice --resource-group keycloak-rg

# List recent deployments
az containerapp revision list --name ceedpodservice --resource-group keycloak-rg
```

## Next Steps

1. **Set up all required secrets** in GitHub repository
2. **Test with a small PR** to verify build process
3. **Merge to dev** to test full deployment
4. **Monitor** the first deployment carefully
5. **Update** your frontend to use the new API URL from the deployment summary

Your CI/CD pipeline is now ready! 🎉