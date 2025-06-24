package com.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for creating or updating a transaction.
 * <p>
 * Used as input for transaction creation and update endpoints.
 */
@Data
public class TransaccionCreateDTO {
    /** Name or description of the transaction. Required. */
    @NotBlank
    private String nombre;

    /** Date of the transaction (yyyy-MM-dd). Required. */
    @NotNull
    private LocalDate fecha;

    /** Monetary value of the transaction. Must be positive. Required. */
    @NotNull
    @DecimalMin(value = "0.01", message = "El valor debe ser positivo")
    private BigDecimal valor;
}