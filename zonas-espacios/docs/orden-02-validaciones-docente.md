# Orden 02 — Validaciones del docente: código de zona y capacidad de espacios

> **Fecha:** 2026-06-01  
> **Objetivo:** Corregir la generación del código de zona para que use abreviatura de 3 letras, y agregar la validación de capacidad al crear un espacio.

---

## Diagnóstico previo a los cambios

| Indicación del docente | Estado encontrado |
|---|---|
| Código de zona formato `ZON-VIP-01`, `ZON-VIS-03` | ⚠️ Con `VISITANTES` generaba `ZON-VISITANTES-01` (muy largo) |
| Validar capacidad de zona al crear espacios | ❌ No existía ninguna validación |
| `eliminarZona` elimina sus espacios hijos | ✅ **Ya funcionaba** gracias a `cascade = CascadeType.ALL, orphanRemoval = true` en la entidad |

---

## Cambio 1 — Abreviatura de 3 letras en `TipoZona.java`

### ¿Cuál era el problema?

El método `generarCodigoZona` en `ServicioZona` recibía el nombre del enum con `.name()`:

```java
// ANTES — usaba el nombre completo del enum
generarCodigoZona(requestDto.getTipo().name(), totalZonas + 1);
// Con VISITANTES generaba → ZON-VISITANTES-01  ← incorrecto
```

El docente pidió un formato corto: `ZON-VIS-01`, `ZON-GEN-02`, `ZON-PRE-03`.

### Solución — agregar campo `abreviatura` al enum

La práctica más limpia y extensible es **enriquecer el propio enum** con la información que necesita. Cada valor del enum lleva su propia abreviatura:

```java
// DESPUÉS — cada tipo carga su propia abreviatura de 3 letras
public enum TipoZona {

    VIP("VIP"),
    VISITANTES("VIS"),
    GENERAL("GEN"),
    PREFERENCIAL("PRE");

    private final String abreviatura;

    TipoZona(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public String getAbreviatura() {
        return abreviatura;
    }
}
```

**¿Por qué en el enum y no en el servicio?** La abreviatura es una propiedad *del tipo mismo*, no del servicio. Ponerla en el enum hace que esté disponible en cualquier clase que use `TipoZona`, sin repetir código.

### Cambio en `ServicioZona.java`

```java
// ANTES
generarCodigoZona(requestDto.getTipo().name(), totalZonas + 1);
//  → ZON-VISITANTES-01

// DESPUÉS
generarCodigoZona(requestDto.getTipo().getAbreviatura(), totalZonas + 1);
//  → ZON-VIS-01  ✅
```

### Tabla de resultados por tipo

| TipoZona enviado en el request | Código generado |
|---|---|
| `VIP` | `ZON-VIP-01` |
| `VISITANTES` | `ZON-VIS-01` |
| `GENERAL` | `ZON-GEN-01` |
| `PREFERENCIAL` | `ZON-PRE-01` |

---

## Cambio 2 — Validación de capacidad en `ServicioEspacio.java`

### ¿Cuál era el problema?

El método `crearEspacio` contaba los espacios existentes de la zona, pero **nunca comparaba ese número contra la capacidad máxima** de la zona. Esto permitía agregar infinitos espacios sin ningún límite.

```java
// ANTES — sin ninguna validación
int totalEspacios = espacioRepositorio.findByZona(zona).size();
int numeroEspacio = totalEspacios + 1;
// Se creaba el espacio sin verificar si la zona ya está llena ❌
```

### Solución — comparar espacios actuales vs. capacidad antes de guardar

```java
// DESPUÉS — con validación de capacidad
int totalEspacios = espacioRepositorio.findByZona(zona).size();

// Si los espacios existentes ya llegan o superan la capacidad, se rechaza la operación
if (totalEspacios >= zona.getCapacidad()) {
    throw new RuntimeException(
        "La zona '" + zona.getNombre() + "' ya alcanzó su capacidad máxima de "
        + zona.getCapacidad() + " espacio(s). No se pueden agregar más."
    );
}

int numeroEspacio = totalEspacios + 1;
```

### ¿Por qué `>=` y no `>`?

- La `capacidad` de la zona es el **máximo de espacios permitidos**.
- Si la capacidad es `5` y ya hay `5` espacios, `totalEspacios (5) >= capacidad (5)` es verdadero → se lanza el error.
- Si usáramos `>`, con 5 espacios existentes y capacidad 5, la condición sería falsa y se crearía un **6to espacio**, violando la restricción.

### Ejemplo de flujo

```
Zona "Zona Norte", capacidad = 3
  → Crear espacio 1: totalEspacios=0 < 3 → OK → "Zon-AUTO-01-001"
  → Crear espacio 2: totalEspacios=1 < 3 → OK → "Zon-AUTO-01-002"
  → Crear espacio 3: totalEspacios=2 < 3 → OK → "Zon-AUTO-01-003"
  → Crear espacio 4: totalEspacios=3 >= 3 → ❌ RuntimeException:
      "La zona 'Zona Norte' ya alcanzó su capacidad máxima de 3 espacio(s)."
```

---

## Punto que ya funcionaba — Eliminación en cascada

El docente mencionó que al eliminar una zona deben eliminarse también sus espacios. Esto **ya estaba configurado correctamente** en `Zona.java` desde la sesión anterior:

```java
// Zona.java — línea 51
@OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<Espacio> espacios = new ArrayList<>();
```

| Anotación | Efecto |
|---|---|
| `cascade = CascadeType.ALL` | Cualquier operación sobre la Zona (guardar, actualizar, **eliminar**) se propaga a sus Espacios |
| `orphanRemoval = true` | Si un Espacio se desvincula de su Zona (queda huérfano), JPA lo elimina automáticamente de la base de datos |

Cuando se llama `zonaRepositorio.deleteById(id)`, JPA ejecuta internamente algo equivalente a:
```sql
DELETE FROM Espacios WHERE id_zona = ?;   -- primero los hijos
DELETE FROM zonas WHERE id = ?;           -- luego el padre
```

---

## Resumen de archivos modificados

| Archivo | Acción | Descripción |
|---|---|---|
| `entidades/TipoZona.java` | ✏️ Modificado | Agregado campo `abreviatura` con `VIP`, `VIS`, `GEN`, `PRE` |
| `servicios/impl/ServicioZona.java` | ✏️ Modificado | `generarCodigoZona` ahora usa `.getAbreviatura()` |
| `servicios/impl/ServicioEspacio.java` | ✏️ Modificado | Validación de capacidad antes de crear espacio |
| `docs/orden-02-validaciones-docente.md` | ✅ Creado | Este documento |
