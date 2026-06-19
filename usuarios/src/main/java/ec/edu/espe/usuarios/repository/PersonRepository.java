package ec.edu.espe.usuarios.repository;

import ec.edu.espe.usuarios.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByPhone(String phone);

    java.util.Optional<Person> findByDni(String dni);

    java.util.Optional<Person> findByEmail(String email);
}