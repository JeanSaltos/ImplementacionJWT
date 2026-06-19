package ec.edu.espe.usuarios.data;

import ec.edu.espe.usuarios.entity.Person;
import ec.edu.espe.usuarios.entity.Role;
import ec.edu.espe.usuarios.entity.User;
import ec.edu.espe.usuarios.entity.UserRole;
import ec.edu.espe.usuarios.entity.UserRoleId;
import ec.edu.espe.usuarios.repository.PersonRepository;
import ec.edu.espe.usuarios.repository.RoleRepository;
import ec.edu.espe.usuarios.repository.UserRepository;
import ec.edu.espe.usuarios.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /** Roles iniciales del sistema. Se crean solo si no existen. */
    private static final List<String[]> ROLES_INICIALES = List.of(
        new String[]{"ADMIN",           "Acceso total: usuarios, roles, zonas, espacios, vehículos, configuración"},
        new String[]{"OPERADOR",        "Gestión de tickets, reservas, ingresos y salidas del parqueadero"},
        new String[]{"SUPERVISOR",      "Consultas, reportes y estadísticas de ocupación"},
        new String[]{"CLIENTE",         "Reservas y consultas de sus propios tickets"},
        new String[]{"SERVICE_TICKETS", "Token interno para llamadas del microservicio tickets hacia zonas-espacios"},
        new String[]{"AUDITOR",         "Solo lectura sobre todos los recursos"}
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Sembrar Roles
        int rolesCreados = 0;
        for (String[] rolData : ROLES_INICIALES) {
            String nombre = rolData[0];
            if (!roleRepository.existsByName(nombre)) {
                roleRepository.save(
                    Role.builder()
                        .name(nombre)
                        .description(rolData[1])
                        .active(true)
                        .build()
                );
                rolesCreados++;
                log.info("[DataSeeder] Rol creado: {}", nombre);
            }
        }

        // 2. Sembrar Usuario Administrador por Defecto
        if (!userRepository.existsByUsername("admin")) {
            // Obtener rol ADMIN
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado"));

            // Intentar buscar la persona por DNI o Email para reusarla y evitar conflictos
            Person person = personRepository.findByDni("9999999999")
                .orElse(null);

            if (person == null) {
                person = personRepository.findByEmail("admin@parqueaderito.com")
                    .orElse(null);
            }

            if (person == null) {
                // Generar un número de teléfono que no esté duplicado
                String safePhone = "0999999998";
                if (personRepository.existsByPhone(safePhone)) {
                    int counter = 1;
                    while (personRepository.existsByPhone("0999999" + String.format("%03d", counter))) {
                        counter++;
                    }
                    safePhone = "0999999" + String.format("%03d", counter);
                }

                person = Person.builder()
                    .dni("9999999999")
                    .firstName("Admin")
                    .middleName("System")
                    .lastName("Parqueaderito")
                    .email("admin@parqueaderito.com")
                    .phone(safePhone)
                    .address("Oficina Central")
                    .nationality("Ecuatoriana")
                    .active(true)
                    .build();
                person = personRepository.save(person);
            }

            // Crear User
            User user = User.builder()
                .person(person)
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin")) // contraseña por defecto: admin
                .active(true)
                .build();
            user = userRepository.save(user);

            // Asignar Rol
            UserRoleId userRoleId = new UserRoleId(user.getId(), adminRole.getId());
            UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role(adminRole)
                .active(true)
                .build();
            userRoleRepository.save(userRole);

            log.info("[DataSeeder] Usuario administrador creado (username: admin, password: admin)");
        } else {
            log.info("[DataSeeder] El usuario administrador ya existe.");
        }
    }
}
