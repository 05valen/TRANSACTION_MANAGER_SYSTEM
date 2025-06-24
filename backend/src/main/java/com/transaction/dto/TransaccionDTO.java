package com.transaction.dto;
import com.transaction.entity.EstadoTransaccion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for transferring transaction data to the client.
 * <p>
 * Contains all relevant fields for transaction representation.
 */
@Data
public class TransaccionDTO {
    /** Unique identifier of the transaction. */
    private Long id;
    /** Name or description of the transaction. */
    private String nombre;
    /** Date of the transaction (yyyy-MM-dd). */
    private LocalDate fecha;
    /** Monetary value of the transaction. */
    private BigDecimal valor;
    /** Current state of the transaction (PENDIENTE or PAGADO). */
    private EstadoTransaccion estado;
}
