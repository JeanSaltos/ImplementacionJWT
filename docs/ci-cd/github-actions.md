# GitHub Actions

## Objetivo

Automatizar la integracion continua del monorepo sin modificar la arquitectura interna de los microservicios.

## Workflow principal

Archivo: `.github/workflows/ci.yml`

El pipeline se ejecuta en:

- Push a `feature/**`, `dev`, `test` y `main`.
- Pull request hacia `dev`, `test` y `main`.

## Etapas

| Job | Responsabilidad |
|---|---|
| `spring-services` | Ejecuta tests Maven en `usuarios` y `zonas-espacios`. |
| `nest-services` | Instala dependencias, ejecuta tests y compila `vehiqlos` y `tickets`. |
| `sonarcloud` | Ejecuta analisis estatico con SonarCloud si existe `SONAR_TOKEN`. |
| `notify` | Envia resultado a Telegram si existen los secretos requeridos. |

## Secretos requeridos

| Secret | Uso |
|---|---|
| `SONAR_TOKEN` | Token de SonarCloud para analisis del proyecto. |
| `TELEGRAM_BOT_TOKEN` | Token del bot de Telegram. |
| `TELEGRAM_CHAT_ID` | Chat o grupo donde se enviaran notificaciones. |

## Decision tecnica

El workflow usa los comandos nativos de cada servicio y no introduce cambios en `pom.xml`, `package.json` ni configuraciones de runtime. Esto permite validar el estado actual del repositorio sin alterar el comportamiento de los microservicios.
