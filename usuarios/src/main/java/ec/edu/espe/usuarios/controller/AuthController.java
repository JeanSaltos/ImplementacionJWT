package ec.edu.espe.usuarios.controller;

import ec.edu.espe.usuarios.dto.request.LoginRequest;
import ec.edu.espe.usuarios.dto.response.AuthResponse;
import ec.edu.espe.usuarios.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoint público de login — no requiere token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida username y password. Retorna un accessToken JWT válido por 60 minutos (desarrollo) que debe enviarse en el header `Authorization: Bearer <token>` en todas las demás peticiones."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso, retorna accessToken"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o usuario inactivo"),
        @ApiResponse(responseCode = "400", description = "Campos requeridos faltantes")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
