# Keycloak Integration Troubleshooting Guide

This document provides comprehensive guidance for resolving Keycloak connection issues and configuring the application for different environments.

## Table of Contents
1. [Quick Start](#quick-start)
2. [Configuration Options](#configuration-options)
3. [Common Issues & Solutions](#common-issues--solutions)
4. [Environment Setups](#environment-setups)
5. [Fallback Mode](#fallback-mode)
6. [Health Check](#health-check)

## Quick Start

### Option 1: Local Keycloak (Recommended for Development)
```bash
# Start local Keycloak with Docker
docker run -p 9080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev

# Start the application with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 2: Fallback Mode (No Keycloak Required)
```bash
# Set environment variables for fallback mode
export KEYCLOAK_ENABLED=false
export KEYCLOAK_FALLBACK_ENABLED=true
export KEYCLOAK_FALLBACK_MODE=development

# Start the application
mvn spring-boot:run
```

## Configuration Options

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `KEYCLOAK_SERVER_URL` | `http://localhost:9080` | Keycloak server URL |
| `KEYCLOAK_REALM_NAME` | `crmAdmin` | Realm name |
| `KEYCLOAK_CLIENT_ID` | `crm-client` | Client ID |
| `KEYCLOAK_CLIENT_SECRET` | `crm-client-secret-2024` | Client secret |
| `KEYCLOAK_ADMIN_USERNAME` | `admin` | Admin username |
| `KEYCLOAK_ADMIN_PASSWORD` | `admin` | Admin password |
| `KEYCLOAK_ENABLED` | `true` | Enable/disable Keycloak integration |
| `KEYCLOAK_FALLBACK_ENABLED` | `true` | Enable fallback mode |
| `KEYCLOAK_FALLBACK_MODE` | `development` | Fallback mode type |
| `KEYCLOAK_CONNECTION_TIMEOUT` | `5000` | Connection timeout in milliseconds |

### Application Properties

```yaml
keycloak:
  server-url: http://localhost:9080
  realm-name: crmAdmin
  client-id: crm-client
  client-secret: crm-client-secret-2024
  admin:
    username: admin
    password: admin
    client-id: admin-cli
  connection:
    timeout: 5000
    enabled: true
  fallback:
    enabled: true
    mode: development
```

## Common Issues & Solutions

### Issue 1: "Database name must not be empty"
**Problem**: MongoDB URI is missing database name
**Solution**: Update MongoDB URI to include database name
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://user:password@cluster.mongodb.net/DATABASE_NAME?retryWrites=true&w=majority
```

### Issue 2: "404 Not Found" when connecting to Keycloak
**Problem**: Keycloak server is not running or URL is incorrect
**Solutions**:
1. **Check if Keycloak is running**: `curl http://localhost:9080/health`
2. **Start local Keycloak**: Use Docker command above
3. **Enable fallback mode**: Set `KEYCLOAK_FALLBACK_ENABLED=true`
4. **Update server URL**: Check your Keycloak deployment URL

### Issue 3: "Connection timeout" or "Connection refused"
**Problem**: Network connectivity issues
**Solutions**:
1. **Check network**: `ping your-keycloak-server.com`
2. **Verify ports**: Ensure port 8080/9080 is accessible
3. **Update timeout**: Increase `KEYCLOAK_CONNECTION_TIMEOUT`
4. **Use fallback mode**: For development without Keycloak

### Issue 4: "401 Unauthorized" admin token errors
**Problem**: Incorrect admin credentials
**Solutions**:
1. **Verify credentials**: Check admin username/password
2. **Check master realm**: Ensure master realm admin exists
3. **Reset Keycloak**: Restart with fresh admin credentials

### Issue 5: Realm creation failures
**Problem**: Permissions or configuration issues
**Solutions**:
1. **Check admin permissions**: Ensure admin has realm management rights
2. **Manual realm creation**: Create realm manually in Keycloak admin console
3. **Use existing realm**: Configure application to use existing realm

## Environment Setups

### Development Environment
```bash
# .env file for development
KEYCLOAK_SERVER_URL=http://localhost:9080
KEYCLOAK_REALM_NAME=crmAdmin
KEYCLOAK_ENABLED=true
KEYCLOAK_FALLBACK_ENABLED=true
KEYCLOAK_FALLBACK_MODE=development
```

### Testing Environment
```bash
# .env file for testing
KEYCLOAK_SERVER_URL=http://test-keycloak:8080
KEYCLOAK_REALM_NAME=crmTest
KEYCLOAK_ENABLED=true
KEYCLOAK_FALLBACK_ENABLED=false
```

### Production Environment
```bash
# .env file for production
KEYCLOAK_SERVER_URL=https://auth.yourdomain.com
KEYCLOAK_REALM_NAME=crmProd
KEYCLOAK_ENABLED=true
KEYCLOAK_FALLBACK_ENABLED=false
KEYCLOAK_CONNECTION_TIMEOUT=10000
```

### Fallback-Only Environment
```bash
# .env file for fallback mode
KEYCLOAK_ENABLED=false
KEYCLOAK_FALLBACK_ENABLED=true
KEYCLOAK_FALLBACK_MODE=testing
```

## Fallback Mode

The application supports fallback mode when Keycloak is unavailable:

### Fallback Modes
- **`development`**: Mock authentication responses, detailed logging
- **`testing`**: Simplified mock responses for testing
- **`disabled`**: No fallback, application fails if Keycloak unavailable

### Fallback Behavior
- **Login**: Returns mock JWT tokens
- **Registration**: Simulates successful user creation
- **Token Refresh**: Returns new mock tokens
- **Logout**: Logs success without Keycloak interaction
- **Realm Operations**: Skipped with informational logging

### Enabling Fallback Mode
```yaml
keycloak:
  fallback:
    enabled: true
    mode: development
```

Or via environment:
```bash
export KEYCLOAK_FALLBACK_ENABLED=true
export KEYCLOAK_FALLBACK_MODE=development
```

## Health Check

### Application Health Endpoint
Check overall application health including Keycloak status:
```bash
curl http://localhost:8084/api/v1/actuator/health
```

### Keycloak-Specific Health Check
```bash
curl http://localhost:8084/api/v1/actuator/health/keycloak
```

### Health Response Example
```json
{
  "status": "UP",
  "components": {
    "keycloak": {
      "status": "DOWN",
      "details": {
        "serverUrl": "http://localhost:9080",
        "realmName": "crmAdmin",
        "enabled": true,
        "available": false,
        "fallbackEnabled": true,
        "fallbackMode": "development",
        "lastChecked": "2025-10-25T10:30:00"
      }
    }
  }
}
```

## Troubleshooting Commands

### Check Keycloak Connectivity
```bash
# Health check
curl http://localhost:9080/health

# Admin console access
curl http://localhost:9080/admin/master/console/

# Realm endpoint
curl http://localhost:9080/realms/crmAdmin
```

### Application Diagnostics
```bash
# View configuration
curl http://localhost:8084/api/v1/actuator/configprops | grep keycloak

# View environment
curl http://localhost:8084/api/v1/actuator/env | grep KEYCLOAK

# Application logs
tail -f logs/application.log | grep -i keycloak
```

### Docker Keycloak Setup
```bash
# Start Keycloak
docker run -d --name keycloak \
  -p 9080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev

# Check logs
docker logs keycloak

# Access admin console
open http://localhost:9080/admin
```

## Support

For additional support:
1. Check application logs in `logs/application.log`
2. Enable DEBUG logging: `logging.level.com.ceedpods.crmbuild=DEBUG`
3. Use health endpoints to diagnose issues
4. Review Keycloak server logs if available

## Configuration Summary

The application now supports:
- ✅ Configurable Keycloak URLs (no hardcoded values)
- ✅ Health checks and connectivity validation
- ✅ Automatic fallback mode when Keycloak unavailable
- ✅ Environment-specific configurations
- ✅ Comprehensive error handling and logging
- ✅ Development-friendly defaults
- ✅ Production-ready security settings