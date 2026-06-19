package ec.edu.espe.zonas.servicios.impl;

import ec.edu.espe.zonas.dto.ZonaRequestDTO;
import ec.edu.espe.zonas.entidades.Espacio;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.entidades.Zona;
import ec.edu.espe.zonas.repositorios.EspacioRepositorio;
import ec.edu.espe.zonas.repositorios.ZonaRepositorio;
import ec.edu.espe.zonas.response.ZonaResponseDto;
import ec.edu.espe.zonas.servicios.interfaz.ZonaServicio;
import ec.edu.espe.zonas.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioZona implements ZonaServicio {
    private final MapperUtils mapper;
    private final ZonaRepositorio zonaRepositorio;
    private final EspacioRepositorio espacioRepositorio;

    @Override
    public List<ZonaResponseDto> listarZonas() {
        return zonaRepositorio.findAll()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ZonaResponseDto obtenerZona(UUID id) {
        Zona zona = zonaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + id));
        return mapearAResponse(zona);
    }

    @Override
    public ZonaResponseDto crear(ZonaRequestDTO requestDto) {
        if (zonaRepositorio.existsByNombre(requestDto.getNombre())) {
            throw new RuntimeException("Ya existe una zona con el nombre: " + requestDto.getNombre());
        }

        // Generar código automático: ZON-[TIPO]-[número secuencial 2 dígitos]
        // Ejemplo: ZON-VIP-01
        int totalZonas = (int) zonaRepositorio.count();
        String codigoGenerado = generarCodigoZona(requestDto.getTipo().getAbreviatura(), totalZonas + 1);

        Zona zona = Zona.builder()
                .nombre(requestDto.getNombre())
                .codigo(codigoGenerado)
                .descripcion(requestDto.getDescripcion())
                .capacidad(requestDto.getCapacidad())
                .tipo(requestDto.getTipo())
                .estado(EstadoEspacio.DISPONIBLE)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Zona guardada = zonaRepositorio.save(zona);
        return mapearAResponse(guardada);
    }

    @Override
    public ZonaResponseDto actualizar(UUID id, ZonaRequestDTO requestDto) {
        Zona zona = zonaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + id));

        if (zonaRepositorio.existsByNombreAndIdNot(requestDto.getNombre(), id)) {
            throw new RuntimeException("Ya existe otra zona con el nombre: " + requestDto.getNombre());
        }

        // Si el tipo de zona cambia, regeneramos el código de la zona
        if (!zona.getTipo().equals(requestDto.getTipo())) {
            String numeroExtraidoStr = extraerNumeroDeZona(zona.getCodigo());
            int numeroExtraido = Integer.parseInt(numeroExtraidoStr);
            String nuevoCodigo = generarCodigoZona(requestDto.getTipo().getAbreviatura(), numeroExtraido);

            if (zonaRepositorio.existsByCodigoAndIdNot(nuevoCodigo, id)) {
                throw new RuntimeException("Ya existe otra zona con el código generado: " + nuevoCodigo);
            }
            zona.setCodigo(nuevoCodigo);
        }

        zona.setNombre(requestDto.getNombre());
        zona.setDescripcion(requestDto.getDescripcion());
        zona.setCapacidad(requestDto.getCapacidad());
        zona.setTipo(requestDto.getTipo());
        zona.setFechaActualizacion(LocalDateTime.now());

        Zona actualizada = zonaRepositorio.save(zona);
        return mapearAResponse(actualizada);
    }

    @Override
    public void eliminarZona(UUID id) {
        Zona zona = zonaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + id));

        // Validar si tiene espacios en estado OCUPADO o RESERVADO
        for (Espacio espacio : zona.getEspacios()) {
            if (EstadoEspacio.OCUPADO.equals(espacio.getEstado()) ||
                EstadoEspacio.RESERVADO.equals(espacio.getEstado())) {
                throw new RuntimeException(
                    "No se puede eliminar la zona '" + zona.getNombre()
                    + "' porque tiene el espacio '" + espacio.getNombre()
                    + "' en estado " + espacio.getEstado() + "."
                );
            }
        }

        zonaRepositorio.delete(zona);
    }

    // ---- Métodos auxiliares privados ----

    /**
     * Genera el código de zona con el formato:
     * ZON-[TIPO]-[número secuencial de 2 dígitos]
     * Ejemplo: ZON-VIP-01
     */
    private String generarCodigoZona(String tipoZona, int numero) {
        String numFormateado = String.format("%02d", numero);
        return "ZON-" + tipoZona + "-" + numFormateado;
    }

    /**
     * Extrae el número final del código de zona.
     * Ejemplo: "ZON-VIP-01" -> "01"
     */
    private String extraerNumeroDeZona(String codigoZona) {
        if (codigoZona != null && codigoZona.contains("-")) {
            String[] partes = codigoZona.split("-");
            return partes[partes.length - 1];
        }
        return "01";
    }

    /**
     * Convierte una entidad Zona a su DTO de respuesta.
     * Calcula el total de espacios disponibles contando los que tienen
     * estado DISPONIBLE en la lista de espacios de la zona.
     */
    private ZonaResponseDto mapearAResponse(Zona zona) {
        // Calcular espacios disponibles: contar los espacios con estado DISPONIBLE
        long disponibles = zona.getEspacios()
                .stream()
                .filter(e -> EstadoEspacio.DISPONIBLE.equals(e.getEstado()))
                .count();

        return ZonaResponseDto.builder()
                .id(zona.getId())
                .nombre(zona.getNombre())
                .codigo(zona.getCodigo())
                .descripcion(zona.getDescripcion())
                .capacidad(zona.getCapacidad())
                .tipo(zona.getTipo())
                .fechaCreacion(zona.getFechaCreacion())
                .fechaActualizacion(zona.getFechaActualizacion())
                .espaciosDisponibles((int) disponibles)
                .build();
    }
}

