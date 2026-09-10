# NavajaBack

Backend Spring Boot para gestión de enlaces, QR, autenticación JWT y utilidades de imagen.

## Endpoints base
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/core/links/create`
- `GET /api/core/links/public/{alias}`
- `GET /api/v1/public/enlaces/{alias}`
- `GET /api/management/links/list`
- `DELETE /api/management/links/{id}`
- `GET /api/v1/tools/qr`
- `POST /api/v1/tools/qr/generate`
- `POST /api/v1/tools/convert-image`

## Documentacion API
- Scalar: `/scalar`
- OpenAPI JSON: `/v3/api-docs`

## Configuracion
Usa `application.yaml` y variables de entorno para base de datos, JWT y frontend.

Variables requeridas en despliegue:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (minimo 32 bytes)
- `FRONTEND_URL` (origen exacto del frontend, por ejemplo `https://app.tudominio.com`)

Variables opcionales:

- `JWT_EXPIRATION_MILLIS` (default `86400000`)
- `PORT` (default `8080` en Docker)

Ejemplo Docker:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/db \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  -e JWT_SECRET=una_clave_muy_larga_de_al_menos_32_bytes \
  -e FRONTEND_URL=https://tu-frontend.com \
  -e PORT=8080 \
  navaja-back
```
