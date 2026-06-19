package ec.edu.espe.zonas.controladores;

import ec.edu.espe.zonas.dto.EspacioRequestDTO;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.response.EspacioResponseDto;
import ec.edu.espe.zonas.servicios.interfaz.EspacioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@Tag(name = "Espacios", description = "Gestión de espacios individuales de parqueo dentro de las zonas")
public class EspacioControlador {

    private final EspacioServicio servicioEspacios;

    // LISTAR TODOS LOS ESPACIOS
    @GetMapping
    @Operation(summary = "Listar todos los espacios", description = "Retorna todos los espacios de parqueo registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de espacios")
    public ResponseEntity<List<EspacioResponseDto>> listarEspacios() {
        return ResponseEntity.ok(servicioEspacios.obtenerEspacios());
    }

    // OBTENER ESPACIO POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener espacio por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos del espacio"),
        @ApiResponse(responseCode = "500", description = "Espacio no encontrado")
    })
    public ResponseEntity<EspacioResponseDto> obtenerEspacio(
            @Parameter(description = "UUID del espacio") @PathVariable UUID id) {
        return ResponseEntity.ok(servicioEspacios.obtenerEspacio(id));
    }

    // CREAR ESPACIO
    @PostMapping
    @Operation(summary = "Crear espacio", description = "Registra un nuevo espacio de parqueo dentro de una zona.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Espacio creado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Zona no encontrada o datos inválidos")
    })
    public ResponseEntity<EspacioResponseDto> crearEspacio(@Valid @RequestBody EspacioRequestDTO dto) {
        EspacioResponseDto responseDto = servicioEspacios.crearEspacio(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // ACTUALIZAR ESPACIO
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar espacio", description = "Actualiza los datos de un espacio de parqueo.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Espacio actualizado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Espacio no encontrado")
    })
    public ResponseEntity<EspacioResponseDto> actualizarEspacio(
            @Parameter(description = "UUID del espacio") @PathVariable UUID id,
            @Valid @RequestBody EspacioRequestDTO dto) {
        return ResponseEntity.ok(servicioEspacios.actualizarEspacio(id, dto));
    }

    // ELIMINAR ESPACIO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar espacio", description = "Elimina permanentemente un espacio de parqueo.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Espacio eliminado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Espacio no encontrado")
    })
    public ResponseEntity<Void> eliminarEspacio(
            @Parameter(description = "UUID del espacio") @PathVariable UUID id) {
        servicioEspacios.eliminarEspacio(id);
        return ResponseEntity.noContent().build();
    }

    // ACTUALIZAR SOLO EL ESTADO DEL ESPACIO
    @PatchMapping("/{id}/estado/{estado}")
    @Operation(summary = "Actualizar estado de espacio", description = "Cambia el estado de un espacio: DISPONIBLE, OCUPADO o RESERVADO. Usado internamente por el microservicio de tickets.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Espacio no encontrado")
    })
    public ResponseEntity<EspacioResponseDto> actualizarEstado(
            @Parameter(description = "UUID del espacio") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado: DISPONIBLE, OCUPADO o RESERVADO") @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(servicioEspacios.actualizarEstado(id, estado));
    }

    // FILTRAR ESPACIOS POR ESTADO
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Filtrar espacios por estado", description = "Retorna todos los espacios con el estado indicado.")
    @ApiResponse(responseCode = "200", description = "Lista de espacios filtrada por estado")
    public ResponseEntity<List<EspacioResponseDto>> espaciosPorEstado(
            @Parameter(description = "Estado a filtrar: DISPONIBLE, OCUPADO o RESERVADO") @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(servicioEspacios.espaciosPorEstado(estado));
    }

    // FILTRAR ESPACIOS POR ZONA Y ESTADO
    @GetMapping("/zona/{idZona}/estado/{estado}")
    @Operation(summary = "Filtrar espacios por zona y estado")
    @ApiResponse(responseCode = "200", description = "Lista de espacios filtrada por zona y estado")
    public ResponseEntity<List<EspacioResponseDto>> espaciosPorZonaYEstado(
            @Parameter(description = "UUID de la zona") @PathVariable UUID idZona,
            @Parameter(description = "Estado a filtrar: DISPONIBLE, OCUPADO o RESERVADO") @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(servicioEspacios.obtenerEspaciosPorZonaYEstado(idZona, estado));
    }

    // ESTADÍSTICAS: CONTEO DE ESPACIOS POR ESTADO AGRUPADOS POR ZONA
    @GetMapping("/estadisticas/{estado}")
    @Operation(summary = "Estadísticas de espacios por estado", description = "Retorna el conteo de espacios agrupados por zona para un estado dado.")
    @ApiResponse(responseCode = "200", description = "Mapa con nombre de zona → cantidad de espacios")
    public ResponseEntity<Map<String, Long>> estadisticasPorEstado(
            @Parameter(description = "Estado a consultar: DISPONIBLE, OCUPADO o RESERVADO") @PathVariable EstadoEspacio estado) {
        return ResponseEntity.ok(servicioEspacios.obtenerEspaciosPorEstadoAgrupadosPorZona(estado));
    }
}
