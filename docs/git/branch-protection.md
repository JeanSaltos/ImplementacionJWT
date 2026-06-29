# Flujo Git y Branch Protection

## Flujo de ramas

```text
feature/*
   -> dev
   -> test
   -> main
```

## Politica de ramas

| Rama | Proposito | Reglas |
|---|---|---|
| `feature/*` | Desarrollo de una actividad o cambio puntual | Se integra mediante Pull Request a `dev`. |
| `dev` | Integracion de desarrollo | Requiere CI exitoso antes de avanzar. |
| `test` | Validacion previa a produccion | Solo recibe cambios desde `dev`. |
| `main` | Version estable | Debe estar protegida. |

## Configuracion recomendada en GitHub

En GitHub:

1. Ir a `Settings`.
2. Entrar a `Branches`.
3. Crear una regla para `main`.
4. Activar:
   - Require a pull request before merging.
   - Require approvals.
   - Dismiss stale pull request approvals when new commits are pushed.
   - Require status checks to pass before merging.
   - Require branches to be up to date before merging.
   - Require conversation resolution before merging.
   - Block force pushes.
   - Block deletions.
5. Agregar como checks requeridos:
   - `Spring Boot services (usuarios)`
   - `Spring Boot services (zonas-espacios)`
   - `NestJS services (vehiqlos)`
   - `NestJS services (tickets)`
   - `SonarCloud analysis`

## Reglas adicionales recomendadas

- Prohibir push directo a `main`.
- Prohibir merge directo a `main`.
- Exigir Pull Request para todo cambio.
- Exigir al menos una aprobacion.
- Exigir CI exitoso.
- Exigir Quality Gate de SonarCloud.

## Nota

La proteccion real se configura en GitHub, no dentro del repositorio local. Este documento define la politica para aplicarla manualmente en el repositorio remoto.
