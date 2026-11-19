package com.tiagoperon.taskservice.business.mapper;


import com.tiagoperon.taskservice.business.dto.TarefasDTO;
import com.tiagoperon.taskservice.infrastructure.entity.TarefasEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TarefasConverter {


    TarefasEntity paraTarefaEntity(TarefasDTO dto);

    TarefasDTO paraTarefaDTO(TarefasEntity entity);

    List<TarefasEntity> paraListaTarefasEntity(List<TarefasDTO> dtos);

    List<TarefasDTO> paraListaTarefasDTO(List<TarefasEntity> entities);
}
