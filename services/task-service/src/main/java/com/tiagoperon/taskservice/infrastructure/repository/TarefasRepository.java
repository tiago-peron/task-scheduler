package com.tiagoperon.taskservice.infrastructure.repository;


import com.tiagoperon.taskservice.infrastructure.entity.TarefasEntity;
import com.tiagoperon.taskservice.infrastructure.enums.StatusNotificacaoEnum;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefasRepository extends MongoRepository<TarefasEntity, String> {

    List<TarefasEntity> findByDataEventoBetweenAndStatusNotificacaoEnum(Instant dataInicial,
                                                                        Instant dataFinal,
                                                                        StatusNotificacaoEnum status);

    List<TarefasEntity> findByEmailUsuario(String email);


}
