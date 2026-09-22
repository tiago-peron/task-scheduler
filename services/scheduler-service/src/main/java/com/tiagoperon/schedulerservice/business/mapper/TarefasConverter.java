package com.tiagoperon.schedulerservice.business.mapper;


import com.tiagoperon.schedulerservice.business.dto.TarefasDTO;
import com.tiagoperon.schedulerservice.infrastructure.entity.TarefasEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TarefasConverter {


    TarefasEntity paraTarefaEntity(TarefasDTO dto);

    TarefasDTO paraTarefaDTO(TarefasEntity entity);

    List<TarefasEntity> paraListaTarefasEntity(List<TarefasDTO> dtos);

    List<TarefasDTO> paraListaTarefasDTO(List<TarefasEntity> entities);
}
