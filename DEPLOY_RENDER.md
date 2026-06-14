# Render deployment

This project is already running in production on Azure, so Render is only kept as reference documentation.

Use the same environment variables as `application.yaml`:

```
DB_URL=jdbc:postgresql://your-db-host:5432/navaja_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_production_jwt_secret_at_least_32_chars
JWT_EXPIRATION_MILLIS=86400000
FRONTEND_URL=https://your-frontend-domain.vercel.app
PORT=8080
```

Useful checks:

```bash
curl https://your-host/actuator/health
curl "https://your-host/api/v1/tools/qr?url=https://google.com"
```
