package com.tiagoperon.bfftaskscheduler.business;



import com.tiagoperon.bfftaskscheduler.business.dto.out.TarefasDTOResponse;
import com.tiagoperon.bfftaskscheduler.infrastructure.client.EmailClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailClient emailClient;

    public void enviaEmail(TarefasDTOResponse dto) {
       emailClient.enviarEmail(dto);
    }


}
