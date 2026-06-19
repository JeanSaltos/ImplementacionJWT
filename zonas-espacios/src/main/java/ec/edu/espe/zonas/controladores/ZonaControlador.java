package ec.edu.espe.zonas.controladores;

import ec.edu.espe.zonas.dto.ZonaRequestDTO;
import ec.edu.espe.zonas.response.ZonaResponseDto;
import ec.edu.espe.zonas.servicios.interfaz.ZonaServicio;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
@Tag(name = "Zonas", description = "Gestión de zonas del parqueadero")
public class ZonaControlador {

    private final ZonaServicio servicioZonas;

    // LISTAR TODAS LAS ZONAS
    @GetMapping
    @Operation(summary = "Listar todas las zonas", description = "Retorna todas las zonas del parqueadero con el conteo de espacios disponibles.")
    @ApiResponse(responseCode = "200", description = "Lista de zonas")
    public ResponseEntity<List<ZonaResponseDto>> listarZonas() {
        return ResponseEntity.ok(servicioZonas.listarZonas());
    }

    // OBTENER ZONA POR ID
    @GetMapping("/{idZona}")
    @Operation(summary = "Obtener zona por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos de la zona"),
        @ApiResponse(responseCode = "500", description = "Zona no encontrada")
    })
    public ResponseEntity<ZonaResponseDto> obtenerZona(
            @Parameter(description = "UUID de la zona") @PathVariable UUID idZona) {
        return ResponseEntity.ok(servicioZonas.obtenerZona(idZona));
    }

    // CREAR ZONA
    @PostMapping
    @Operation(summary = "Crear zona", description = "Crea una nueva zona en el parqueadero. El código se genera automáticamente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Zona creada exitosamente"),
        @ApiResponse(responseCode = "500", description = "Ya existe una zona con ese nombre")
    })
    public ResponseEntity<ZonaResponseDto> crearZona(@Valid @RequestBody ZonaRequestDTO dto) {
        ZonaResponseDto responseDto = servicioZonas.crear(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // ACTUALIZAR ZONA
    @PutMapping("/{idZona}")
    @Operation(summary = "Actualizar zona", description = "Actualiza los datos de una zona existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zona actualizada exitosamente"),
        @ApiResponse(responseCode = "500", description = "Zona no encontrada o nombre en uso")
    })
    public ResponseEntity<ZonaResponseDto> actualizarZona(
            @Parameter(description = "UUID de la zona") @PathVariable UUID idZona,
            @Valid @RequestBody ZonaRequestDTO dto) {
        return ResponseEntity.ok(servicioZonas.actualizar(idZona, dto));
    }

    // ELIMINAR ZONA
    @DeleteMapping("/{idZona}")
    @Operation(summary = "Eliminar zona", description = "Elimina una zona. No se puede eliminar si tiene espacios OCUPADOS o RESERVADOS.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Zona eliminada exitosamente"),
        @ApiResponse(responseCode = "500", description = "Zona no encontrada o tiene espacios activos")
    })
    public ResponseEntity<Void> eliminarZona(
            @Parameter(description = "UUID de la zona") @PathVariable UUID idZona) {
        servicioZonas.eliminarZona(idZona);
        return ResponseEntity.noContent().build();
    }
}
