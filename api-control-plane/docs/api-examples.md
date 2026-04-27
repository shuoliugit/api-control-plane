# API Examples

## Register and Login

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"dev@example.com","password":"Password123!","displayName":"Dev User"}'
```

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@controlplane.local","password":"AdminPassword123!"}'
```

## Create an Application

```bash
curl -X POST http://localhost:8080/api/apps \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Treasury Sandbox","description":"Payment API certification app"}'
```

## Generate Credentials

```bash
curl -X POST http://localhost:8080/api/apps/$APP_ID/credentials \
  -H "Authorization: Bearer $TOKEN"
```

## Call Sandbox API

```bash
curl http://localhost:8080/api/sandbox/accounts \
  -H "X-Client-Id: $CLIENT_ID" \
  -H "X-Client-Secret: $CLIENT_SECRET" \
  -H "X-Request-Id: demo-request-001"
```

Interactive OpenAPI documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```
