package com.tiagoperon.notificationservice.business.dto;

import com.tiagoperon.notificationservice.business.enums.StatusNotificacaoEnum;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TarefasDTO {

    private String id;
    private String nomeTarefa;
    private String descricao;
    private Instant dataCriacao;
    private Instant dataEvento;
    private String emailUsuario;
    private Instant dataAlteracao;
    private StatusNotificacaoEnum statusNotificacaoEnum;

}
