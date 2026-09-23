package com.tiagoperon.bfftaskscheduler.infrastructure.client;


import com.tiagoperon.bfftaskscheduler.business.dto.in.EnderecoDTOResquest;
import com.tiagoperon.bfftaskscheduler.business.dto.in.LoginRequestDTO;
import com.tiagoperon.bfftaskscheduler.business.dto.in.TelefoneDTORequest;
import com.tiagoperon.bfftaskscheduler.business.dto.in.UsuarioDTORequest;
import com.tiagoperon.bfftaskscheduler.business.dto.out.EnderecoDTOResponse;
import com.tiagoperon.bfftaskscheduler.business.dto.out.TelefoneDTOResponse;
import com.tiagoperon.bfftaskscheduler.business.dto.out.UsuarioDTOResponse;
import com.tiagoperon.bfftaskscheduler.business.dto.out.ViaCepDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario", url = "${external-services.user-service.url}")
public interface UsuarioClient {

    @GetMapping
    UsuarioDTOResponse buscaUsuarioPorEmail(@RequestParam("email") String email,
                                            @RequestHeader(value = "Authorization", required=false) String token );

    @PostMapping
    UsuarioDTOResponse salvaUsuario(@RequestBody UsuarioDTORequest usuarioDTO);


    @PostMapping("/login")
    String login(@RequestBody LoginRequestDTO usuarioDTO);


    @DeleteMapping("/{email}")
    void deletaUsuarioPorEmail(@PathVariable String email,
                               @RequestHeader(value = "Authorization", required=false) String token );

    @PutMapping
    UsuarioDTOResponse atualizDadoUsuario(@RequestBody UsuarioDTORequest dto,
                                          @RequestHeader(value = "Authorization", required=false) String token );


    @PutMapping("/endereco")
    EnderecoDTOResponse atualizaEndereco(@RequestBody EnderecoDTOResquest dto,
                                         @RequestParam("id") Long id,
                                         @RequestHeader(value = "Authorization", required=false) String token);


    @PutMapping("/telefone")
    TelefoneDTOResponse atualizaTelefone(@RequestBody TelefoneDTORequest dto,
                                         @RequestParam("id") Long id,
                                         @RequestHeader(value = "Authorization", required=false) String token );


    @PostMapping("/endereco")
    EnderecoDTOResponse cadastaEndereco(@RequestBody EnderecoDTOResquest dto,
                                        @RequestHeader(value = "Authorization", required=false) String token );


    @PostMapping("/telefone")
    TelefoneDTOResponse cadastraTelefone(@RequestBody TelefoneDTORequest dto,
                                         @RequestHeader(value = "Authorization", required=false) String token);

    @GetMapping("/endereco/{cep}")
    ViaCepDTOResponse buscarDadosCep(@PathVariable("cep") String cep);




}
