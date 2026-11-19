package com.tiagoperon.userservice.infrastructure.exceptions.dto;

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
public class ErrorResponseDTO {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
