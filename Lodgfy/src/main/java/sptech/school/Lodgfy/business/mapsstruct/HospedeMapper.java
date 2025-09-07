package sptech.school.Lodgfy.business.mapsstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sptech.school.Lodgfy.business.dto.HospedeRequestDTO;
import sptech.school.Lodgfy.business.dto.HospedeResponseDTO;
import sptech.school.Lodgfy.infrastructure.entities.HospedeEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospedeMapper {

    @Mapping(target = "id", ignore = true)
    HospedeEntity paraHospedeEntity(HospedeRequestDTO request);

    HospedeResponseDTO paraHospedeResponseDTO(HospedeEntity entity);

    List<HospedeResponseDTO> paraListaHospedeResponseDTO(List<HospedeEntity> entities);
}
