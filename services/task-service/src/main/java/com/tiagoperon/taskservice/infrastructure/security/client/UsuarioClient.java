package com.tiagoperon.taskservice.infrastructure.security.client;

import com.tiagoperon.taskservice.business.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario", url = "${external-services.user-service.url}", path = "/usuario")
public interface UsuarioClient {

    @GetMapping()
    UsuarioDTO buscaUsuarioPorEmail(@RequestParam("email") String email,
                                    @RequestHeader("Authorization") String token);

}
