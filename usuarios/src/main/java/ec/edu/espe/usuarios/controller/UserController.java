package ec.edu.espe.usuarios.controller;

import ec.edu.espe.usuarios.dto.request.UserCreateRequest;
import ec.edu.espe.usuarios.dto.response.UserResponse;
import ec.edu.espe.usuarios.services.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema de parqueo")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario con su información personal. El username se genera automáticamente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "DNI o email ya registrado, o datos inválidos")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userRequest) {
        return new ResponseEntity<>(userService.createUser(userRequest), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios registrados")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos del usuario"),
        @ApiResponse(responseCode = "400", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "UUID del usuario") @PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos personales de un usuario (nombre, apellido, email, teléfono, dirección, nacionalidad). El DNI no se puede modificar.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Usuario no encontrado o email en uso")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "UUID del usuario") @PathVariable UUID id,
            @Valid @RequestBody UserCreateRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina permanentemente un usuario del sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID del usuario") @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Asignar rol a usuario", description = "Asigna un rol existente al usuario. Si ya tiene el rol asignado, no genera error.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol asignado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Usuario o rol no encontrado")
    })
    public ResponseEntity<UserResponse> assignRole(
            @Parameter(description = "UUID del usuario") @PathVariable UUID id,
            @Parameter(description = "UUID del rol") @PathVariable UUID roleId) {
        return ResponseEntity.ok(userService.assignRole(id, roleId));
    }
}
