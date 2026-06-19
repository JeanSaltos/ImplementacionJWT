package ec.edu.espe.usuarios.controller;

import ec.edu.espe.usuarios.dto.request.RoleCreateRequest;
import ec.edu.espe.usuarios.dto.response.RoleResponse;
import ec.edu.espe.usuarios.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles de usuario en el sistema de parqueo")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Crear rol", description = "Registra un nuevo rol en el sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El nombre del rol ya existe")
    })
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleCreateRequest roleRequest) {
        return new ResponseEntity<>(roleService.createRole(roleRequest), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los roles")
    @ApiResponse(responseCode = "200", description = "Lista de roles disponibles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getRoles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos del rol"),
        @ApiResponse(responseCode = "400", description = "Rol no encontrado")
    })
    public ResponseEntity<RoleResponse> getRoleById(
            @Parameter(description = "UUID del rol") @PathVariable UUID id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualiza el nombre y descripción de un rol existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rol no encontrado o nombre en uso")
    })
    public ResponseEntity<RoleResponse> updateRole(
            @Parameter(description = "UUID del rol") @PathVariable UUID id,
            @RequestBody RoleCreateRequest roleRequest) {
        return ResponseEntity.ok(roleService.updateRole(id, roleRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina permanentemente un rol del sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Rol eliminado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Rol no encontrado")
    })
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "UUID del rol") @PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
