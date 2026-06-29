# Arquitectura actual

## Resumen

ParkingApp esta organizado como un monorepo con cuatro microservicios:

- `usuarios`
- `zonas-espacios`
- `vehiqlos`
- `tickets`

## Comunicacion actual

El servicio `tickets` consume informacion de usuarios, vehiculos y espacios mediante HTTP directo configurado por variables de entorno.

## Seguridad actual

La autenticacion se centraliza en `usuarios`, que emite JWT. Los servicios `zonas-espacios`, `vehiqlos` y `tickets` validan el token para proteger operaciones internas.

## Infraestructura agregada en Actividad 1

- README raiz.
- Documentacion tecnica en `docs/`.
- Ejemplos `.env.example`.
- Docker Compose base para bases de datos.
- Workflow GitHub Actions.
- Configuracion base SonarCloud.
- Documentacion de Telegram y branch protection.

## Restricciones de esta actividad

No se modifica la arquitectura interna, logica de negocio, configuracion JWT, persistencia, dependencias ni comunicacion entre microservicios.
