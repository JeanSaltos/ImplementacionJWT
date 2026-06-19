# Orden 03 — Validación de eliminación y función actualizarEstado

> **Fecha:** 2026-06-01  
> **Objetivo:** Agregar validación de estado al eliminar un espacio, crear la función `actualizarEstado`, exponer el nuevo endpoint PATCH y actualizar la colección Postman.

---

## Diagnóstico previo a los cambios

| Funcionalidad | Estado encontrado |
|---|---|
| Validar que no se elimine un espacio OCUPADO/RESERVADO | ❌ No existía — `eliminarEspacio` solo verificaba que el espacio existiera |
| Función `actualizarEstado` | ❌ No existía — solo existía `actualizarEspacio` que modifica todos los campos |
| Colección Postman actualizada | ❌ Faltaba el nuevo endpoint PATCH y las nuevas descripciones de reglas de negocio |

---

## Cambio 1 — Validación de estado en `eliminarEspacio`

### ¿Por qué no se puede eliminar un espacio OCUPADO o RESERVADO?

Tiene sentido de negocio: si un espacio está **OCUPADO**, hay un vehículo dentro. Eliminarlo de la base de datos sin que salga el vehículo generaría inconsistencias en el sistema. Lo mismo con **RESERVADO**: alguien pagó o reservó ese lugar.

### Antes (sin validación)

```java
public void eliminarEspacio(UUID id) {
    if (!espacioRepositorio.existsById(id)) {
        throw new RuntimeException("Espacio no encontrado con id: " + id);
    }
    espacioRepositorio.deleteById(id); // Se eliminaba sin importar el estado
}
```

### Después (con validación)

```java
public void eliminarEspacio(UUID id) {
    Espacio espacio = espacioRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));

    // VALIDAR ESTADO: no se puede eliminar un espacio OCUPADO o RESERVADO
    if (EstadoEspacio.OCUPADO.equals(espacio.getEstado()) ||
        EstadoEspacio.RESERVADO.equals(espacio.getEstado())) {
        throw new RuntimeException(
            "No se puede eliminar el espacio '" + espacio.getNombre()
            + "' porque su estado actual es: " + espacio.getEstado()
            + ". Cambie el estado a DISPONIBLE o MANTENIMIENTO antes de eliminar."
        );
    }
    espacioRepositorio.deleteById(id);
}
```

> [!NOTE]
> Nótese el cambio de patrón: antes usábamos `existsById` (solo devuelve `boolean`) y luego `deleteById`. Ahora usamos `findById` que **nos da el objeto completo**, lo que nos permite leer su estado antes de decidir si eliminamos o no.

### Flujo correcto para eliminar un espacio OCUPADO

```
1. PATCH /api/espacios/{id}/estado/DISPONIBLE  ← liberar primero
2. DELETE /api/espacios/{id}                  ← ahora sí se puede eliminar
```

---

## Cambio 2 — Nueva función `actualizarEstado`

### ¿Por qué no usar el `PUT` (actualizarEspacio) ya existente?

El `PUT /api/espacios/{id}` actualiza **todos** los campos: tipo, estado, descripción e idZona. Para un operador que solo quiere marcar un espacio como "OCUPADO" cuando entra un carro, pedirle que envíe el cuerpo completo del espacio es innecesario y propenso a errores.

La convención REST establece:
- **PUT** → reemplaza el recurso completo (necesita todos los campos)
- **PATCH** → actualiza parcialmente (solo los campos que cambias)

### Implementación en la interfaz

```java
// EspacioServicio.java
EspacioResponseDto actualizarEstado(UUID id, EstadoEspacio nuevoEstado);
```

### Implementación en el servicio

```java
// ServicioEspacio.java
public EspacioResponseDto actualizarEstado(UUID id, EstadoEspacio nuevoEstado) {
    Espacio espacio = espacioRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));

    espacio.setEstado(nuevoEstado);
    espacio.setFechaActualizacion(LocalDateTime.now());

    Espacio actualizado = espacioRepositorio.save(espacio);
    return mapearAResponse(actualizado);
}
```

### Endpoint en el controlador

```java
// EspacioControlador.java
@PatchMapping("/{id}/estado/{estado}")
public ResponseEntity<EspacioResponseDto> actualizarEstado(
        @PathVariable UUID id,
        @PathVariable EstadoEspacio estado) {
    return ResponseEntity.ok(servicioEspacios.actualizarEstado(id, estado));
}
```

**Ruta:** `PATCH /api/espacios/{id}/estado/{nuevoEstado}`

**Ejemplos de uso:**
```
PATCH /api/espacios/550e8400.../estado/OCUPADO      ← entra un auto
PATCH /api/espacios/550e8400.../estado/DISPONIBLE   ← sale el auto
PATCH /api/espacios/550e8400.../estado/MANTENIMIENTO ← en reparación
```

No requiere body — toda la información va en la URL. Esto lo hace especialmente rápido de usar desde aplicaciones cliente.

---

## Cambio 3 — Colección Postman actualizada (v2)

Se actualizó `zonas-espacios-postman.json` con:

- **Request 5 nuevo** en carpeta Espacios: `PATCH /{id}/estado/{estado}` — Actualizar solo el estado
- **Request 6 DELETE** actualizado con la descripción de la regla de negocio y el flujo correcto previo
- **Request 3 POST** actualizado con la descripción de la validación de capacidad
- **Request 2 POST Crear zona** actualizado con la tabla de códigos abreviados (VIS, GEN, PRE)
- **Descripción general** actualizada con las dos nuevas reglas de negocio

---

## Resumen de todos los endpoints de Espacios (estado final)

| # | Método | URL | Descripción |
|---|---|---|---|
| 1 | GET | `/api/espacios` | Listar todos |
| 2 | GET | `/api/espacios/{id}` | Obtener por ID |
| 3 | POST | `/api/espacios` | Crear (valida capacidad) |
| 4 | PUT | `/api/espacios/{id}` | Actualizar completo |
| 5 | **PATCH** | `/api/espacios/{id}/estado/{estado}` | **Actualizar solo estado** ← NUEVO |
| 6 | DELETE | `/api/espacios/{id}` | Eliminar (bloquea si OCUPADO/RESERVADO) |
| 7 | GET | `/api/espacios/estado/{estado}` | Filtrar por estado |
| 8 | GET | `/api/espacios/zona/{idZona}/estado/{estado}` | Filtrar por zona y estado |
| 9 | GET | `/api/espacios/estadisticas/{estado}` | Conteo agrupado por zona |

## Resumen de archivos modificados

| Archivo | Acción |
|---|---|
| `servicios/interfaz/EspacioServicio.java` | ✏️ Agregado método `actualizarEstado` |
| `servicios/impl/ServicioEspacio.java` | ✏️ Validación en `eliminarEspacio` + implementación de `actualizarEstado` |
| `controladores/EspacioControlador.java` | ✏️ Nuevo endpoint `@PatchMapping("/{id}/estado/{estado}")` |
| `docs/zonas-espacios-postman.json` | ✏️ Actualizado a v2 con el nuevo request y descripciones |
| `docs/orden-03-estado-validacion-patch.md` | ✅ Creado — este documento |
