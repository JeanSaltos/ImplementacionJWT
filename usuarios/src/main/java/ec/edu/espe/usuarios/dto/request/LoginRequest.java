package ec.edu.espe.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales para inicio de sesión")
public class LoginRequest {

    @NotBlank(message = "El username es requerido")
    @Schema(description = "Nombre de usuario", example = "jlopez")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Schema(description = "Contraseña del usuario", example = "1712345678")
    private String password;
}
