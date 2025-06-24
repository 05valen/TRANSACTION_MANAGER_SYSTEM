package com.transaction.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Result of a payment processing operation.
 * Contains information about how many transactions were paid and remaining amount.
 * 
 * @author Transaction Management System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    
    /**
     * Number of transactions that were successfully paid.
     */
    private int transaccionesPagadas;
    
    /**
     * Remaining amount after payment processing.
     */
    private BigDecimal montoRestante;
    
    /**
     * Original amount that was attempted to pay.
     */
    private BigDecimal montoInicial;
    
    /**
     * Amount required to pay the first pending transaction that couldn't be paid.
     */
    private BigDecimal montoRequerido;
    
    /**
     * Gets a user-friendly message describing the payment result.
     * 
     * @return Formatted message about the payment result
     */
    public String getMensaje() {
        // Caso 1: No hay transacciones pendientes
        if (transaccionesPagadas == 0 && montoRequerido == null) {
            return "No hay transacciones pendientes para pagar.";
        }
        
        // Caso 2: Se pagaron transacciones exitosamente
        if (transaccionesPagadas > 0) {
            BigDecimal montoPagado = montoInicial.subtract(montoRestante);
            
            if (montoRestante.compareTo(BigDecimal.ZERO) == 0) {
                // Pago exacto - no sobró dinero
                return "✅ Pago exitoso. Se pagaron " + transaccionesPagadas + 
                       " transacción(es) por un total de $" + montoPagado + ".";
            } else {
                // Pago parcial - sobró dinero
                return "✅ Pago parcial exitoso. Se pagaron " + transaccionesPagadas + 
                       " transacción(es) por $" + montoPagado + 
                       ". Sobró $" + montoRestante + " que no se pudo usar.";
            }
        }
        
        // Caso 3: No se pudo pagar ninguna transacción
        if (transaccionesPagadas == 0 && montoRequerido != null) {
            // Verificar si es por excedente o por monto insuficiente
            if (montoInicial.compareTo(montoRequerido) > 0) {
                // Hay excedente - el monto es mayor al requerido exacto
                return "❌ Pago rechazado. El monto $" + montoInicial + 
                       " excede el monto exacto requerido de $" + montoRequerido + 
                       ". Solo se permiten pagos exactos.";
            } else {
                // Monto insuficiente
                return "⚠️ Monto insuficiente. El monto $" + montoInicial + 
                       " no alcanza para pagar la transacción más antigua que requiere $" + montoRequerido + 
                       ". Necesitas pagar exactamente $" + montoRequerido + " o más.";
            }
        }
        
        // Caso por defecto (no debería llegar aquí)
        return "Resultado del pago: " + transaccionesPagadas + " transacciones pagadas, $" + montoRestante + " restante.";
    }
} 