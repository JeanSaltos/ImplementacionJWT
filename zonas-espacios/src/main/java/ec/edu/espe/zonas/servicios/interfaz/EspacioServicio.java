package ec.edu.espe.zonas.servicios.interfaz;

import ec.edu.espe.zonas.dto.EspacioRequestDTO;
import ec.edu.espe.zonas.entidades.EstadoEspacio;
import ec.edu.espe.zonas.response.EspacioResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EspacioServicio {
    List<EspacioResponseDto> obtenerEspacios();

    EspacioResponseDto crearEspacio(EspacioRequestDTO requestDto);

    EspacioResponseDto actualizarEspacio(UUID id, EspacioRequestDTO requestDto);

    void eliminarEspacio(UUID id);

    // Actualiza únicamente el estado de un espacio (DISPONIBLE, OCUPADO, RESERVADO, MANTENIMIENTO)
    EspacioResponseDto actualizarEstado(UUID id, EstadoEspacio nuevoEstado);

    EspacioResponseDto obtenerEspacio(UUID id);

    List<EspacioResponseDto> espaciosPorEstado(EstadoEspacio estado);

    List<EspacioResponseDto> obtenerEspaciosPorZonaYEstado(UUID idZona, EstadoEspacio estado);

    // Obtiene el conteo de espacios por estado agrupados por zona
    // Retorna un mapa donde la clave es el nombre de la zona y el valor es la cantidad de espacios
    Map<String, Long> obtenerEspaciosPorEstadoAgrupadosPorZona(EstadoEspacio estado);
}

