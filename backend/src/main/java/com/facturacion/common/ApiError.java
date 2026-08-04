package com.facturacion.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;
}
