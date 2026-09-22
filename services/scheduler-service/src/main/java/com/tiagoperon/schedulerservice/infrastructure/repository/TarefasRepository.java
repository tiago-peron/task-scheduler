package com.tiagoperon.schedulerservice.infrastructure.repository;


import com.tiagoperon.schedulerservice.infrastructure.entity.TarefasEntity;
import com.tiagoperon.schedulerservice.infrastructure.enums.StatusNotificacaoEnum;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefasRepository extends MongoRepository<TarefasEntity, String> {

    List<TarefasEntity> findByDataEventoBetweenAndStatusNotificacaoEnum(LocalDateTime dataInicial,
                                                                        LocalDateTime dataFinal,
                                                                        StatusNotificacaoEnum status);

    List<TarefasEntity> findByEmailUsuario(String email);


}
