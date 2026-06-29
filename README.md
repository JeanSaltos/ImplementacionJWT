# ParkingApp

ParkingApp es una plataforma de gestion de parqueaderos basada en microservicios. El objetivo del proyecto es evolucionar hacia una solucion SaaS multitenant, manteniendo aislamiento de informacion por empresa sin introducir todavia cambios en la logica interna de los servicios.

## Estado actual

| Componente | Tecnologia | Ruta | Responsabilidad |
|---|---|---|---|
| usuarios | Spring Boot, Java, Maven, PostgreSQL | `usuarios/` | Usuarios, roles y autenticacion JWT |
| zonas-espacios | Spring Boot, Java, Maven, MySQL | `zonas-espacios/` | Zonas y espacios de parqueo |
| vehiqlos | NestJS, TypeScript, TypeORM, PostgreSQL | `vehiqlos/` | Gestion de vehiculos |
| tickets | NestJS, TypeScript, TypeORM, PostgreSQL | `tickets/` | Tickets, reservas, ingreso y salida |

Componentes planificados:

- `api-gateway`
- `frontend`

## Estructura del repositorio

```text
ParkingApp/
  .github/workflows/      # Automatizaciones CI
  docs/                   # Documentacion tecnica y decisiones
  tickets/                # Microservicio NestJS de tickets
  usuarios/               # Microservicio Spring Boot de usuarios/auth
  vehiqlos/               # Microservicio NestJS de vehiculos
  zonas-espacios/         # Microservicio Spring Boot de zonas/espacios
  docker-compose.yml      # Infraestructura local base
  sonar-project.properties
```

## Ejecucion local

1. Crear archivos `.env` locales a partir de los ejemplos:

```bash
cp .env.example .env
cp usuarios/.env.example usuarios/.env
cp zonas-espacios/.env.example zonas-espacios/.env
cp vehiqlos/.env.example vehiqlos/.env
cp tickets/.env.example tickets/.env
```

2. Levantar bases de datos locales:

```bash
docker compose up -d
```

3. Ejecutar cada microservicio desde su carpeta con sus comandos nativos.

## Flujo Git

El flujo definido para el proyecto es:

```text
feature/*
   -> dev
   -> test
   -> main
```

- `feature/*`: desarrollo de cambios puntuales.
- `dev`: integracion continua de cambios aprobados.
- `test`: validacion previa a produccion.
- `main`: rama estable y protegida.

Ver [docs/git/branch-protection.md](docs/git/branch-protection.md).

## CI/CD

El workflow principal esta en `.github/workflows/ci.yml` e incluye:

- Build y tests para servicios Spring Boot.
- Build y tests para servicios NestJS.
- Analisis SonarCloud.
- Notificaciones Telegram al finalizar el pipeline.

Ver [docs/ci-cd/github-actions.md](docs/ci-cd/github-actions.md), [docs/ci-cd/sonarcloud.md](docs/ci-cd/sonarcloud.md) y [docs/ci-cd/telegram.md](docs/ci-cd/telegram.md).

## Multitenancy

La estrategia SaaS multitenant queda documentada como decision futura. En esta etapa no se implementan entidad `Tenant`, `tenant_id`, filtros, cambios de JWT ni cambios de base de datos.

Ver [docs/adr/0001-multitenancy-strategy.md](docs/adr/0001-multitenancy-strategy.md).
