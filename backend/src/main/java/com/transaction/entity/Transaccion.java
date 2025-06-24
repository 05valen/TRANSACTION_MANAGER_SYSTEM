package com.transaction.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a transaction record in the system.
 * <p>
 * Maps to the 'transacciones' table in the database.
 */
@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaccion {

    /** Unique identifier for the transaction (auto-generated). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name or description of the transaction. Cannot be null. */
    @Column(nullable = false)
    private String nombre;

    /** Date of the transaction. Cannot be null. */
    @Column(nullable = false)
    private LocalDate fecha;

    /** Monetary value of the transaction. Cannot be null. */
    @Column(nullable = false)
    private BigDecimal valor;

    /**
     * Current state of the transaction.
     * <ul>
     *   <li>PENDIENTE: Pending payment</li>
     *   <li>PAGADO: Paid</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estado;
}