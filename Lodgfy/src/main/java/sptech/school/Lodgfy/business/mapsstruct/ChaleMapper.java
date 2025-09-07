package sptech.school.Lodgfy.business.mapsstruct;

import org.mapstruct.Mapper;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChaleMapper {

    ChaleEntity paraChaleEntity(ChaleRequestDTO dto);

    ChaleResponseDTO paraChaleResponseDTO(ChaleEntity entity);

    List<ChaleResponseDTO> paraListaChaleResponseDTO(List<ChaleEntity> entities);
}