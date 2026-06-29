# ADR 0001: Estrategia futura de multitenancy

## Estado

Propuesta para fases futuras.

## Contexto

ParkingApp debe evolucionar hacia una plataforma SaaS multitenant donde una sola aplicacion atienda a multiples empresas manteniendo aislados sus usuarios, vehiculos, zonas, espacios, tickets y configuraciones.

## Decision

La estrategia recomendada para las siguientes fases es iniciar con multitenancy por columna usando un identificador de tenant en las entidades de negocio, validado desde el contexto de autenticacion y aplicado de forma obligatoria en consultas y escrituras.

## Alcance excluido en Actividad 1

En esta actividad no se implementa:

- Entidad `Tenant`.
- Campo `tenant_id`.
- JWT multitenant.
- Roles multitenant.
- Filtros por tenant.
- Cambios de base de datos.
- Cambios en microservicios.

## Lineamientos futuros

- Definir una entidad central de tenant/empresa.
- Incluir el tenant en los claims JWT.
- Resolver el tenant desde el gateway o desde el contexto de seguridad.
- Aplicar aislamiento en repositorios y servicios.
- Evitar endpoints que permitan consultar datos sin tenant.
- Agregar pruebas de aislamiento entre tenants.
- Evaluar migraciones controladas antes de introducir cambios de esquema.

## Consecuencias

Esta decision mantiene bajo el costo operativo inicial y permite evolucionar de forma incremental. Si en el futuro se requieren requisitos regulatorios mas fuertes, se podra evaluar aislamiento por esquema o por base de datos.
