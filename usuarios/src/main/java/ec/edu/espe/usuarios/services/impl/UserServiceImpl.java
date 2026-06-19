package ec.edu.espe.usuarios.services.impl;

import ec.edu.espe.usuarios.dto.request.UserCreateRequest;
import ec.edu.espe.usuarios.dto.response.PersonResponse;
import ec.edu.espe.usuarios.dto.response.UserResponse;
import ec.edu.espe.usuarios.entity.Person;
import ec.edu.espe.usuarios.entity.User;
import ec.edu.espe.usuarios.entity.UserRole;
import ec.edu.espe.usuarios.repository.PersonRepository;
import ec.edu.espe.usuarios.repository.UserRepository;
import ec.edu.espe.usuarios.repository.UserRoleRepository;
import ec.edu.espe.usuarios.repository.RoleRepository;
import ec.edu.espe.usuarios.entity.Role;
import ec.edu.espe.usuarios.entity.UserRoleId;
import ec.edu.espe.usuarios.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest userRequest) {

        // Validaciones de unicidad
        if (personRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("El email ya existe");
        }
        if (personRepository.existsByDni(userRequest.getDni())) {
            throw new IllegalArgumentException("La identificación DNI ya existe");
        }

        // Creación de Person
        Person person = Person.builder()
                .dni(userRequest.getDni())
                .firstName(userRequest.getFirstName())
                .middleName(userRequest.getMiddleName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .phone(userRequest.getPhone())
                .address(userRequest.getAddress())
                .nationality(userRequest.getNationality())
                .build();

        person = personRepository.save(person);

        String firstLetter = userRequest.getFirstName().trim().substring(0, 1).toLowerCase();
        String secondLetter = userRequest.getMiddleName() != null && !userRequest.getMiddleName().trim().isEmpty()
                ? userRequest.getMiddleName().trim().substring(0, 1).toLowerCase()
                : "";
        String firstLastName = userRequest.getLastName().trim().split("\\s+")[0].toLowerCase();

        String baseUsername = firstLetter + secondLetter + firstLastName;
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        User user = User.builder()
                .person(person)
                .username(username)
                .passwordHash(passwordEncoder.encode(userRequest.getDni()))
                .build();

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse assignRole(UUID id, UUID roleId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

        UserRoleId userRoleId = new UserRoleId(id, roleId);
        
        if (!userRoleRepository.existsById(userRoleId)) {
            UserRole userRole = UserRole.builder()
                    .id(userRoleId)
                    .user(user)
                    .role(role)
                    .active(true)
                    .build();
            userRoleRepository.save(userRole);
        }

        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserCreateRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Person person = user.getPerson();

        // Validar unicidad de email si cambió
        if (!person.getEmail().equals(userRequest.getEmail()) &&
                personRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        // Actualizar solo campos no clave de negocio
        person.setFirstName(userRequest.getFirstName());
        if (userRequest.getMiddleName() != null) person.setMiddleName(userRequest.getMiddleName());
        person.setLastName(userRequest.getLastName());
        person.setEmail(userRequest.getEmail());
        if (userRequest.getPhone() != null) person.setPhone(userRequest.getPhone());
        if (userRequest.getAddress() != null) person.setAddress(userRequest.getAddress());
        if (userRequest.getNationality() != null) person.setNationality(userRequest.getNationality());

        personRepository.save(person);
        return mapToUserResponse(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        userRepository.delete(user);
    }

    private UserResponse mapToUserResponse(User user) {
        List<String> roles = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        Person person = user.getPerson();

        PersonResponse personResponse = PersonResponse.builder()
                .id(person.getId())
                .dni(person.getDni())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .phone(person.getPhone())
                .address(person.getAddress())
                .nationality(person.getNationality())
                .active(person.getActive())
                .build();

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .active(user.getActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .person(personResponse)
                .roles(roles)
                .build();
    }
}