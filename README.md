# API Gateway

Nginx-based API Gateway for the Library Booking System microservices.

## Overview

The API Gateway provides a single entry point (`http://localhost:8080`) for all API requests, routing them to the appropriate microservices.

## Routes

| Route | Service | Port |
|-------|---------|------|
| `/api/auth` | auth-service | 3002 |
| `/api/users` | user-service | 3001 |
| `/api/resources` | catalog-service | 3003 |
| `/api/bookings` | booking-service | 3004 |
| `/api/policies` | policy-service | 3005 |
| `/api/notifications` | notification-service | 3006 |
| `/api/analytics` | analytics-service | 3007 |
| `/ws/` | realtime-gateway | 3008 (optional) |

## Features

- **Request Routing**: Routes API requests to appropriate microservices
- **CORS Support**: Configured for Flutter web app cross-origin requests
- **WebSocket Support**: Proxy support for real-time WebSocket connections
- **Health Check**: `/health` endpoint for gateway status

## Building and Running

The API Gateway is automatically built and started with docker-compose:

```bash
cd docker-compose
docker-compose up -d api-gateway
```

Or build it separately:

```bash
cd api-gateway
docker build -t library-api-gateway .
```

## Configuration

Edit `nginx.conf` to modify routing rules, add new services, or change CORS settings.

## Testing

Test the gateway:

```bash
# Health check
curl http://localhost:8080/health

# Test auth endpoint
curl http://localhost:8080/api/auth/health

# Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123","role":"STUDENT"}'
```

## Troubleshooting

### Gateway won't start
- Check that all microservices are running
- Verify port 8080 is not in use
- Check logs: `docker-compose logs api-gateway`

### Requests timing out
- Verify microservices are healthy: `docker-compose ps`
- Check nginx error logs: `docker exec library-api-gateway cat /var/log/nginx/error.log`

### CORS issues
- Verify CORS headers in `nginx.conf`
- Check browser console for specific CORS errors

