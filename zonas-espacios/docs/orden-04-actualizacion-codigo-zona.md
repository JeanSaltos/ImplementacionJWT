# Orden 04 — Actualización Automática del Código de Zona al Cambiar de Tipo

> **Fecha:** 2026-06-01  
> **Objetivo:** Corregir el bug que impedía actualizar el código identificador de la zona (ej. de `ZON-VIP-01` a `ZON-GEN-01`) cuando el tipo de zona era modificado durante una actualización.

---

## Diagnóstico del Problema

En el diseño original del microservicio, el código de la zona se generaba automáticamente al crearla (ej. `ZON-VIP-01` si era de tipo `VIP`). Sin embargo, en el método `actualizar` de `ServicioZona.java`, aunque se actualizaban campos como nombre, descripción, capacidad y tipo, **nunca se actualizaba el campo `codigo`**. 

Si un docente o administrador cambiaba el tipo de una zona de `VIP` a `GENERAL`, la base de datos registraba el cambio de tipo, pero el código permanecía como `ZON-VIP-01`, lo cual genera inconsistencias en la nomenclatura del sistema.

### Comportamiento anterior
```
Request PUT (Tipo: GENERAL) ──> [actualizar()] ──> Guarda Tipo: GENERAL, pero Código: ZON-VIP-01 (Sin cambios)
```

### Comportamiento deseado
```
Request PUT (Tipo: GENERAL) ──> [actualizar()] ──> Identifica cambio de tipo ──> Regenera Código: ZON-GEN-01 ──> Guarda ambos
```

---

## Solución Implementada

Para resolver este problema de manera limpia y sin alterar el número secuencial correlativo de la zona (en el ejemplo, el `01`), se implementaron los siguientes cambios:

### 1. Detección de Colisiones de Código
Dado que el código de zona es un campo único en la base de datos, antes de guardar un nuevo código modificado debemos asegurarnos de que ninguna otra zona lo esté usando.

Se agregó el método de consulta en [ZonaRepositorio.java](file:///j:/ProyectoP1/zonas-espacios/src/main/java/ec/edu/espe/zonas/repositorios/ZonaRepositorio.java):
```java
boolean existsByCodigoAndIdNot(String codigo, UUID id);
```
Este método valida si existe alguna **otra** zona (diferente a la que estamos actualizando, comparando por su ID) que ya posea el mismo código generado, previniendo excepciones de violación de restricción única de SQL.

### 2. Extracción del Número Secuencial Existente
Para no alterar el número de la zona (ej: si es la zona 1, debe seguir siendo la número 1 tras el cambio de tipo), creamos un método auxiliar en `ServicioZona.java` para extraer la parte numérica final de su código actual:
```java
private String extraerNumeroDeZona(String codigoZona) {
    if (codigoZona != null && codigoZona.contains("-")) {
        String[] partes = codigoZona.split("-");
        return partes[partes.length - 1]; // Devuelve el último segmento, ej: "01"
    }
    return "01";
}
```

### 3. Actualización de Lógica en `actualizar()`
En [ServicioZona.java](file:///j:/ProyectoP1/zonas-espacios/src/main/java/ec/edu/espe/zonas/servicios/impl/ServicioZona.java), modificamos el método `actualizar` para evaluar si el tipo ha cambiado y, en ese caso, regenerar el código conservando el número correlativo:

```java
// Si el tipo de zona cambia, regeneramos el código de la zona
if (!zona.getTipo().equals(requestDto.getTipo())) {
    // 1. Extraer el secuencial de la zona actual (ej. "01" de "ZON-VIP-01")
    String numeroExtraidoStr = extraerNumeroDeZona(zona.getCodigo());
    int numeroExtraido = Integer.parseInt(numeroExtraidoStr);
    
    // 2. Generar el nuevo código con la abreviatura del nuevo tipo (ej. "GEN" -> "ZON-GEN-01")
    String nuevoCodigo = generarCodigoZona(requestDto.getTipo().getAbreviatura(), numeroExtraido);

    // 3. Validar colisión única
    if (zonaRepositorio.existsByCodigoAndIdNot(nuevoCodigo, id)) {
        throw new RuntimeException("Ya existe otra zona con el código generado: " + nuevoCodigo);
    }
    
    // 4. Asignar el nuevo código
    zona.setCodigo(nuevoCodigo);
}
```

---

## Verificación

### Pasos para probar el cambio en Postman:
1. **Obtener la lista de zonas** o crear una zona con `tipo: VIP` (recibirá el código `ZON-VIP-XX`).
2. Realizar un `PUT /api/zonas/{id}` enviando el cuerpo de la zona pero cambiando `"tipo": "GENERAL"`.
3. Validar que la respuesta retorne un código de respuesta HTTP `200 OK` y el atributo `"codigo"` del JSON sea `"ZON-GEN-XX"` (manteniendo el mismo número correlativo `XX`).
4. Llamar a `GET /api/zonas` y constatar que el cambio de código se ha guardado correctamente en la base de datos.
