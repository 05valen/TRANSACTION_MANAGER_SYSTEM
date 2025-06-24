package com.transaction.controller;

import com.transaction.dto.TransaccionCreateDTO;
import com.transaction.dto.TransaccionDTO;
import com.transaction.entity.EstadoTransaccion;
import com.transaction.service.PaymentResult;
import com.transaction.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing transactions.
 * <p>
 * Provides endpoints for CRUD operations, filtering, and payment processing.
 * Business rules:
 * <ul>
 *   <li>Transactions in 'PAGADO' (paid) state cannot be edited or deleted.</li>
 *   <li>Payments are applied in chronological order and only if the amount covers the full transaction.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransaccionController {

    private final TransaccionService transaccionService;

    /**
     * Retrieves a list of transactions, optionally filtered by name, date, and state.
     *
     * @param nombre Optional name filter (partial match, case-insensitive)
     * @param fecha  Optional exact date filter (yyyy-MM-dd)
     * @param estado Optional state filter (PENDIENTE or PAGADO)
     * @return List of matching transactions
     */
    @GetMapping
    public List<TransaccionDTO> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) EstadoTransaccion estado
    ) {
        return transaccionService.listar(nombre, fecha, estado);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id Transaction ID
     * @return 200 with transaction if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDTO> obtenerPorId(@PathVariable Long id) {
        return transaccionService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new transaction. The initial state is always PENDIENTE.
     *
     * @param dto Transaction data (name, date, value)
     * @return The created transaction
     */
    @PostMapping
    public ResponseEntity<TransaccionDTO> crear(@Valid @RequestBody TransaccionCreateDTO dto) {
        return ResponseEntity.ok(transaccionService.crear(dto));
    }

    /**
     * Updates an existing transaction. Only allowed if the transaction is not paid.
     *
     * @param id  Transaction ID
     * @param dto New transaction data
     * @return The updated transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransaccionDTO> editar(@PathVariable Long id, @Valid @RequestBody TransaccionCreateDTO dto) {
        return ResponseEntity.ok(transaccionService.editar(id, dto));
    }

    /**
     * Deletes a transaction by ID. Only allowed if the transaction is not paid.
     *
     * @param id Transaction ID
     * @return 204 No Content if deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transaccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Processes a payment, marking transactions as paid in chronological order if the amount covers them fully.
     * 
     * El usuario ingresa un monto a pagar. El sistema paga automáticamente las transacciones pendientes
     * en orden de antigüedad, solo si el monto cubre completamente cada una, tal como lo pide la prueba técnica.
     *
     * @param monto Amount to pay
     * @return Message with detailed payment result and appropriate HTTP status code
     */
    @PostMapping("/pagar")
    public ResponseEntity<String> pagar(@RequestParam BigDecimal monto) {
        PaymentResult resultado = transaccionService.pagar(monto);
        String mensaje = resultado.getMensaje();
        
        // Determinar el código HTTP apropiado basándose en el mensaje
        if (mensaje.startsWith("✅ Pago exitoso")) {
            // Pago exitoso - 200 OK
            return ResponseEntity.ok(mensaje);
        } else if (mensaje.startsWith("✅ Pago parcial exitoso")) {
            // Pago parcial exitoso - 200 OK
            return ResponseEntity.ok(mensaje);
        } else if (mensaje.startsWith("No hay transacciones pendientes")) {
            // No hay transacciones para pagar - 200 OK (no es un error)
            return ResponseEntity.ok(mensaje);
        } else if (mensaje.startsWith("❌ Pago rechazado")) {
            // Pago rechazado por excedente - 422 Unprocessable Entity (lógica de negocio)
            return ResponseEntity.unprocessableEntity().body(mensaje);
        } else if (mensaje.startsWith("⚠️ Monto insuficiente")) {
            // Monto insuficiente - 400 Bad Request (el usuario debería saber cuánto necesita)
            return ResponseEntity.badRequest().body(mensaje);
        } else {
            // Caso por defecto - 200 OK
            return ResponseEntity.ok(mensaje);
        }
    }
}