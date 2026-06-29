# Docker Compose local

## Objetivo

Proveer infraestructura local base para bases de datos sin modificar los microservicios.

## Archivo

- `docker-compose.yml`

## Servicios incluidos

| Servicio Compose | Motor | Puerto host por defecto | Uso |
|---|---|---:|---|
| `usuarios-db` | PostgreSQL 16 | `5432` | Base de datos de `usuarios` |
| `vehiqlos-db` | PostgreSQL 16 | `5433` | Base de datos de `vehiqlos` |
| `tickets-db` | PostgreSQL 16 | `5434` | Base de datos de `tickets` |
| `zonas-espacios-db` | MySQL 8.4 | `3306` | Base de datos de `zonas-espacios` |

## Uso

```bash
cp .env.example .env
docker compose up -d
docker compose ps
docker compose down
```

Para eliminar volumenes locales:

```bash
docker compose down -v
```

## Decision tecnica

En esta actividad no se agregan Dockerfiles de aplicacion ni se cambia el empaquetado de los microservicios. El Compose queda limitado a infraestructura compartida para desarrollo local.
