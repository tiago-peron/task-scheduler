package com.tiagoperon.userservice.business;

import com.tiagoperon.userservice.infrastructure.clients.ViaCepClient;
import com.tiagoperon.userservice.infrastructure.clients.ViaCepDTO;
import com.tiagoperon.userservice.infrastructure.exceptions.ResourceNotFoundException;
import java.time.Duration;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class ViaCepService {

    private final ViaCepClient client;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ViaCepDTO buscarDadosEndereco(String cep) {

        try {
            String cepLimpo = processarCep(cep);
            String key = "enderecos" + cepLimpo;

            String dadosCacheRedis = redisTemplate.opsForValue().get(key);

            if (Objects.nonNull(dadosCacheRedis)) {

                return objectMapper.readValue(dadosCacheRedis, ViaCepDTO.class);
            }

            ViaCepDTO dto = client.buscaDadosEndereco(cepLimpo);

            if (Objects.nonNull(dto) && Objects.nonNull(dto.getCep())) {
                String stringDTO = objectMapper.writeValueAsString(dto);

                redisTemplate.opsForValue().set(key, stringDTO, Duration.ofDays(5));
            }

            return dto;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Erro ao obter cep", e);
        }
    }

    private String processarCep(String cep) {
        String cepFormatado = cep.replace(" ", "").
                replace("-", "");

        if (!cepFormatado.matches("\\d+")
                || !Objects.equals(cepFormatado.length(), 8)) {
            throw new IllegalArgumentException("O cep contém caracteres inválidos, favor verificar");
        }

        return cepFormatado;
    }
}
