# Orden 05 — Validación de Estado de Espacios al Eliminar una Zona

> **Fecha:** 2026-06-01  
> **Objetivo:** Impedir la eliminación de una zona si alguno de sus espacios asociados se encuentra activo en estado `OCUPADO` o `RESERVADO`, evitando la pérdida accidental de datos de estacionamiento activos.

---

## Diagnóstico del Problema

El microservicio utiliza una relación `@OneToMany` entre `Zona` y `Espacio` configurada con eliminación en cascada (`cascade = CascadeType.ALL, orphanRemoval = true`).

Anteriormente, al ejecutar el endpoint `DELETE /api/zonas/{id}`, el sistema realizaba directamente un `deleteById(id)` sin comprobar el estado de los espacios hijos. Esto significaba que si una zona contenía espacios ocupados por vehículos (estado `OCUPADO`) o apartados (estado `RESERVADO`), se eliminaban de forma silenciosa de la base de datos, lo cual es inaceptable en una regla de negocio de estacionamientos.

### Flujo incorrecto anterior
```
Petición DELETE ──> [eliminarZona()] ──> deleteById() ──> Cascada de DB elimina todo (incluso espacios Ocupados/Reservados)
```

### Flujo de negocio correcto implementado
```
Petición DELETE ──> [eliminarZona()] 
                        │
                        └───> ¿Tiene algún espacio OCUPADO o RESERVADO?
                                    ├── SI ──> Lanza RuntimeException (Abortar eliminación)
                                    └── NO ──> Elimina la zona y sus espacios vacíos/mantenimiento
```

---

## Solución Implementada

### Modificación en `ServicioZona.java`

Se actualizó el método `eliminarZona(UUID id)` en [ServicioZona.java](file:///j:/ProyectoP1/zonas-espacios/src/main/java/ec/edu/espe/zonas/servicios/impl/ServicioZona.java) para realizar la verificación antes del borrado:

```java
@Override
public void eliminarZona(UUID id) {
    // 1. Cargar la entidad Zona completa desde la BD
    Zona zona = zonaRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + id));

    // 2. Validar el estado de cada uno de sus espacios
    for (Espacio espacio : zona.getEspacios()) {
        if (EstadoEspacio.OCUPADO.equals(espacio.getEstado()) ||
            EstadoEspacio.RESERVADO.equals(espacio.getEstado())) {
            
            // 3. Si se encuentra un espacio activo, se aborta la transacción
            throw new RuntimeException(
                "No se puede eliminar la zona '" + zona.getNombre()
                + "' porque tiene el espacio '" + espacio.getNombre()
                + "' en estado " + espacio.getEstado() + "."
            );
        }
    }

    // 4. Proceder con el borrado si todos los espacios son DISPONIBLE o MANTENIMIENTO
    zonaRepositorio.delete(zona);
}
```

### Conceptos Clave de JPA/Hibernate

- **Carga Perezosa (Lazy Loading)**: La colección `zona.getEspacios()` se carga de forma diferida de la base de datos al ser accedida en el bucle `for`.
- **Integridad de Transacciones**: Al lanzar una `RuntimeException`, la transacción actual de Spring se marca automáticamente como *rollback*, garantizando que ningún cambio parcial se persista en la base de datos si la validación falla.

---

## Plan de Verificación

### Pasos para probar en Postman:
1. **Verificar estado**: Ejecuta `GET /api/espacios` para identificar un espacio con estado `OCUPADO` o `RESERVADO` y apunta el ID de su zona.
2. **Intentar eliminar**: Envía una petición `DELETE /api/zonas/{idZona}` utilizando el ID de esa zona.
3. **Verificar respuesta de error**: El sistema debe responder con un error HTTP indicando el mensaje detallado:
   ```json
   {
     "message": "No se puede eliminar la zona 'Zona Norte' porque tiene el espacio 'Zon-AUTO-01-001' en estado OCUPADO."
   }
   ```
4. **Validación exitosa**: Cambia el estado del espacio a `DISPONIBLE` (usando el endpoint PATCH) o prueba con una zona vacía, realiza el `DELETE` de la zona y constata que retorne un código de respuesta `204 No Content`.
