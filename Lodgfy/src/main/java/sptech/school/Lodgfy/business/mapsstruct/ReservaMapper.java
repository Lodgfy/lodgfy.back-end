package sptech.school.Lodgfy.business.mapsstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sptech.school.Lodgfy.business.dto.ReservaRequestDTO;
import sptech.school.Lodgfy.business.dto.ReservaResponseDTO;
import sptech.school.Lodgfy.infrastructure.entities.ReservaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "idReserva", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "hospede", ignore = true)
    @Mapping(target = "chale", ignore = true)
    ReservaEntity paraReservaEntity(ReservaRequestDTO dto);

    ReservaResponseDTO paraReservaResponseDTO(ReservaEntity entity);

    List<ReservaResponseDTO> paraListaReservaResponseDTO(List<ReservaEntity> entities);
}

