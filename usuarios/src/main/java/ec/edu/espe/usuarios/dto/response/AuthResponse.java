package ec.edu.espe.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Respuesta de autenticación con el token JWT")
public class AuthResponse {

    @Schema(description = "Token JWT de acceso")
    private String accessToken;

    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Duración en segundos del token", example = "3600")
    private long expiresIn;

    @Schema(description = "Nombre de usuario autenticado")
    private String username;

    @Schema(description = "Roles asignados al usuario")
    private List<String> roles;
}
