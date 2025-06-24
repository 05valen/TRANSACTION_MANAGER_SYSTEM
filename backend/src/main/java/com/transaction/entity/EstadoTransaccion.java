package com.transaction.entity;

/**
 * Enum representing the possible states of a transaction.
 * <ul>
 *   <li>PENDIENTE: Transaction is pending payment.</li>
 *   <li>PAGADO: Transaction has been paid and is immutable.</li>
 * </ul>
 */
public enum EstadoTransaccion {
    /** Transaction is pending payment. */
    PENDIENTE,
    /** Transaction has been paid and is immutable. */
    PAGADO
}

