# SonarCloud

## Objetivo

Preparar analisis continuo de calidad para los microservicios de ParkingApp.

## Archivos relacionados

- `sonar-project.properties`
- `.github/workflows/ci.yml`

## Configuracion en GitHub

1. Crear/importar el proyecto en SonarCloud.
2. Confirmar los valores:
   - Organization: `jeansaltos`
   - Project key: `JeanSaltos_ParkingApp`
3. Crear el secret `SONAR_TOKEN` en GitHub:
   - Repository Settings
   - Secrets and variables
   - Actions
   - New repository secret

## Quality Gate recomendado

Para proteger `main`, configurar en SonarCloud un Quality Gate que falle si hay:

- Bugs nuevos.
- Vulnerabilidades nuevas.
- Security hotspots sin revisar.
- Code smells criticos.
- Cobertura insuficiente en codigo nuevo.
- Duplicacion excesiva en codigo nuevo.

## Nota sobre monorepo

Esta configuracion analiza el repositorio como un solo proyecto SonarCloud. Si el proyecto crece y cada microservicio necesita metricas separadas, se puede evolucionar a configuracion por servicio sin cambiar la arquitectura interna.
