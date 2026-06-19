package ec.edu.espe.zonas.utils;

import ec.edu.espe.zonas.dto.ZonaRequestDTO;
import ec.edu.espe.zonas.entidades.Zona;
import ec.edu.espe.zonas.response.ZonaResponseDto;
import org.springframework.stereotype.Component;

@Component
public class MapperUtils {
    public ZonaResponseDto toZonaResponseDto(Zona zona) {
        if (zona == null) {
            return null;
        }
        return ZonaResponseDto.builder()
                .id(zona.getId())
                .nombre(zona.getNombre())
                .codigo(zona.getCodigo())
                .descripcion(zona.getDescripcion())
                .capacidad(zona.getCapacidad())
                .tipo(zona.getTipo())
                .fechaCreacion(zona.getFechaCreacion())
                .fechaActualizacion(zona.getFechaActualizacion())
                .espaciosDisponibles(zona.getEspacios()!= null ? zona.getEspacios().size():0)
                .build();
    }
    public Zona toZonaEntity(ZonaRequestDTO dto){
        if (dto == null) {
            return null;
        }
        return Zona.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .capacidad(dto.getCapacidad())
                .tipo(dto.getTipo())
                .build();
    }


}
