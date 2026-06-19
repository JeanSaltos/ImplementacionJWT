package ec.edu.espe.zonas.repositorios;

import ec.edu.espe.zonas.entidades.Espacio;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.entidades.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EspacioRepositorio extends JpaRepository<Espacio, UUID> {

    // Busca todos los espacios de una zona específica
    List<Espacio> findByZona(Zona zona);

    // Busca espacios de una zona específica filtrados por estado
    List<Espacio> findByZonaAndEstado(Zona zona, EstadoEspacio estado);

    // Busca todos los espacios con un estado determinado (sin importar la zona)
    List<Espacio> findByEstado(EstadoEspacio estado);

    // Busca espacios por estado agrupados por zona
    // Retorna una lista de arreglos: [zona, cantidad de espacios con ese estado]
    @Query("SELECT e.zona, COUNT(e) FROM Espacio e WHERE e.estado = :estado GROUP BY e.zona")
    List<Object[]> findEspaciosPorEstadoAgrupadosPorZona(@Param("estado") EstadoEspacio estado);

}