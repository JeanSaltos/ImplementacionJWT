package ec.edu.espe.zonas.servicios.impl;

import ec.edu.espe.zonas.dto.EspacioRequestDTO;
import ec.edu.espe.zonas.entidades.Espacio;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.entidades.Zona;
import ec.edu.espe.zonas.repositorios.EspacioRepositorio;
import ec.edu.espe.zonas.repositorios.ZonaRepositorio;
import ec.edu.espe.zonas.response.EspacioResponseDto;
import ec.edu.espe.zonas.servicios.interfaz.EspacioServicio;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicioEspacio implements EspacioServicio {

    private final EspacioRepositorio espacioRepositorio;
    private final ZonaRepositorio zonaRepositorio;

    public ServicioEspacio(EspacioRepositorio espacioRepositorio, ZonaRepositorio zonaRepositorio) {
        this.espacioRepositorio = espacioRepositorio;
        this.zonaRepositorio = zonaRepositorio;
    }

    @Override
    public List<EspacioResponseDto> obtenerEspacios() {
        return espacioRepositorio.findAll()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EspacioResponseDto crearEspacio(EspacioRequestDTO requestDto) {
        Zona zona = zonaRepositorio.findById(requestDto.getIdZona())
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + requestDto.getIdZona()));

        // Contar cuántos espacios ya tiene la zona para generar el número secuencial
        int totalEspacios = espacioRepositorio.findByZona(zona).size();

        // VALIDAR CAPACIDAD: no se puede agregar más espacios que la capacidad de la zona
        if (totalEspacios >= zona.getCapacidad()) {
            throw new RuntimeException(
                "La zona '" + zona.getNombre() + "' ya alcanzó su capacidad máxima de "
                + zona.getCapacidad() + " espacio(s). No se pueden agregar más."
            );
        }

        int numeroEspacio = totalEspacios + 1;


        // Generar nombre: [3 letras del nombre de zona]-[TipoEspacio]-[num zona 2 dig]-[num espacio 3 dig]
        // Ejemplo: Zon-AUTO-01-001  (donde "Zon" son las 3 primeras letras del nombre de la zona)
        String nombreGenerado = generarNombreEspacio(zona, requestDto.getTipo().name(), numeroEspacio);

        Espacio espacio = Espacio.builder()
                .nombre(nombreGenerado)
                .tipo(requestDto.getTipo())
                .descripcion(requestDto.getDescripcion())
                .estado(EstadoEspacio.DISPONIBLE)
                .zona(zona)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        Espacio guardado = espacioRepositorio.save(espacio);
        return mapearAResponse(guardado);
    }

    @Override
    public EspacioResponseDto actualizarEspacio(UUID id, EspacioRequestDTO requestDto) {
        Espacio espacio = espacioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));

        espacio.setTipo(requestDto.getTipo());
        espacio.setDescripcion(requestDto.getDescripcion());
        espacio.setFechaActualizacion(LocalDateTime.now());

        Espacio actualizado = espacioRepositorio.save(espacio);
        return mapearAResponse(actualizado);
    }

    @Override
    public void eliminarEspacio(UUID id) {
        Espacio espacio = espacioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));

        // VALIDAR ESTADO: no se puede eliminar un espacio OCUPADO o RESERVADO
        if (EstadoEspacio.OCUPADO.equals(espacio.getEstado()) ||
            EstadoEspacio.RESERVADO.equals(espacio.getEstado())) {
            throw new RuntimeException(
                "No se puede eliminar el espacio '" + espacio.getNombre()
                + "' porque su estado actual es: " + espacio.getEstado()
                + ". Cambie el estado a DISPONIBLE o MANTENIMIENTO antes de eliminar."
            );
        }

        espacioRepositorio.deleteById(id);
    }

    @Override
    public EspacioResponseDto actualizarEstado(UUID id, EstadoEspacio nuevoEstado) {
        Espacio espacio = espacioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));

        espacio.setEstado(nuevoEstado);
        espacio.setFechaActualizacion(LocalDateTime.now());

        Espacio actualizado = espacioRepositorio.save(espacio);
        return mapearAResponse(actualizado);
    }

    @Override
    public EspacioResponseDto obtenerEspacio(UUID id) {
        Espacio espacio = espacioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Espacio no encontrado con id: " + id));
        return mapearAResponse(espacio);
    }

    @Override
    public List<EspacioResponseDto> espaciosPorEstado(EstadoEspacio estado) {
        return espacioRepositorio.findByEstado(estado)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EspacioResponseDto> obtenerEspaciosPorZonaYEstado(UUID idZona, EstadoEspacio estado) {
        Zona zona = zonaRepositorio.findById(idZona)
                .orElseThrow(() -> new RuntimeException("Zona no encontrada con id: " + idZona));
        return espacioRepositorio.findByZonaAndEstado(zona, estado)
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> obtenerEspaciosPorEstadoAgrupadosPorZona(EstadoEspacio estado) {
        List<Object[]> resultados = espacioRepositorio.findEspaciosPorEstadoAgrupadosPorZona(estado);
        Map<String, Long> mapa = new HashMap<>();
        for (Object[] fila : resultados) {
            Zona zona = (Zona) fila[0];
            Long cantidad = (Long) fila[1];
            mapa.put(zona.getNombre(), cantidad);
        }
        return mapa;
    }

    // ---- Métodos auxiliares privados ----

    /**
     * Genera el nombre del espacio con el formato:
     * [3 primeras letras del nombre de la zona]-[TipoEspacio]-[num zona 2 dígitos]-[num espacio 3 dígitos]
     * Ejemplo: Zon-AUTO-01-001
     */
    private String generarNombreEspacio(Zona zona, String tipoEspacio, int numeroEspacio) {
        // Primeras 3 letras del nombre de la zona
        String prefijoZona = zona.getNombre().length() >= 3
                ? zona.getNombre().substring(0, 3)
                : zona.getNombre();
        // Número de zona extraído del código (ZON-VIP-01 → "01")
        String numZona = extraerNumeroDeZona(zona.getCodigo());
        // Número de espacio con 3 dígitos
        String numEspacio = String.format("%03d", numeroEspacio);
        return prefijoZona + "-" + tipoEspacio + "-" + numZona + "-" + numEspacio;
    }

    /**
     * Extrae el número final del código de zona.
     * Ejemplo: "ZON-VIP-01" → "01"
     */
    private String extraerNumeroDeZona(String codigoZona) {
        if (codigoZona != null && codigoZona.contains("-")) {
            String[] partes = codigoZona.split("-");
            return partes[partes.length - 1];
        }
        return "01";
    }

    /**
     * Convierte una entidad Espacio a su DTO de respuesta.
     */
    private EspacioResponseDto mapearAResponse(Espacio espacio) {
        return EspacioResponseDto.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .descripcion(espacio.getDescripcion())
                .tipo(espacio.getTipo())
                .estado(espacio.getEstado())
                .nombreZona(espacio.getZona() != null ? espacio.getZona().getNombre() : null)
                .idZona(espacio.getZona() != null ? espacio.getZona().getId() : null)
                .fechaCreacion(espacio.getFechaCreacion())
                .fechaActualizacion(espacio.getFechaActualizacion())
                .build();
    }
}
