package sptech.school.Lodgfy.business.mapsstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sptech.school.Lodgfy.business.dto.ReservaResponseDTO;
import sptech.school.Lodgfy.infrastructure.entities.ReservaEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(source = "hospede.id", target = "hospedeId")
    @Mapping(source = "hospede.nome", target = "hospedeNome")
    @Mapping(source = "chale.idChale", target = "chaleId")
    @Mapping(source = "chale.numero", target = "chaleNumero")
    @Mapping(source = "chale.nome", target = "chaleNome")
    ReservaResponseDTO paraReservaResponseDTO(ReservaEntity entity);

    List<ReservaResponseDTO> paraListaReservaResponseDTO(List<ReservaEntity> entities);
}

