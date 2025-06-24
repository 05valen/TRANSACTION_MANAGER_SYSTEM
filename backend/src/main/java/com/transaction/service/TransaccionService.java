package com.transaction.service;


import com.transaction.dto.TransaccionCreateDTO;
import com.transaction.dto.TransaccionDTO;
import com.transaction.entity.EstadoTransaccion;
import com.transaction.entity.Transaccion;
import com.transaction.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for transaction business logic.
 * <p>
 * Handles CRUD operations, filtering, and payment processing with business rules enforcement.
 * <ul>
 *   <li>Only transactions in PENDIENTE state can be edited or deleted.</li>
 *   <li>Payments are applied in chronological order and only if the amount covers the full transaction.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TransaccionService {
    private final TransaccionRepository transaccionRepository;

    /**
     * Retrieves a list of transactions filtered by name, date, and state.
     *
     * @param nombre Optional name filter (partial match, case-insensitive)
     * @param fecha  Optional exact date filter
     * @param estado Optional state filter
     * @return List of matching transactions as DTOs
     */
    public List<TransaccionDTO> listar(String nombre, LocalDate fecha, EstadoTransaccion estado) {
        return transaccionRepository.findByFilters(nombre, fecha, estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new transaction with state PENDIENTE.
     *
     * @param dto Data for the new transaction
     * @return The created transaction as DTO
     */
    public TransaccionDTO crear(TransaccionCreateDTO dto) {
        Transaccion t = Transaccion.builder()
                .nombre(dto.getNombre())
                .fecha(dto.getFecha())
                .valor(dto.getValor())
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        return toDTO(transaccionRepository.save(t));
    }

    /**
     * Updates an existing transaction if it is not paid.
     *
     * @param id  Transaction ID
     * @param dto New data for the transaction
     * @return The updated transaction as DTO
     * @throws IllegalArgumentException if transaction not found
     * @throws IllegalStateException    if transaction is already paid
     */
    public TransaccionDTO editar(Long id, TransaccionCreateDTO dto) {
        Transaccion t = transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));
        if (t.getEstado() == EstadoTransaccion.PAGADO) {
            throw new IllegalStateException("No se puede editar una transacción pagada");
        }
        t.setNombre(dto.getNombre());
        t.setFecha(dto.getFecha());
        t.setValor(dto.getValor());
        return toDTO(transaccionRepository.save(t));
    }

    /**
     * Deletes a transaction if it is not paid.
     *
     * @param id Transaction ID
     * @throws IllegalArgumentException if transaction not found
     * @throws IllegalStateException    if transaction is already paid
     */
    public void eliminar(Long id) {
        Transaccion t = transaccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));
        if (t.getEstado() == EstadoTransaccion.PAGADO) {
            throw new IllegalStateException("No se puede eliminar una transacción pagada");
        }
        transaccionRepository.deleteById(id);
    }

    /**
     * Processes a payment, marking transactions as paid in chronological order if the amount covers them fully.
     * 
     * Lógica de pago por lote:
     * - Se pagan transacciones en orden de fecha (más antigua primero)
     * - Solo se paga si el monto cubre completamente la transacción
     * - Si el monto no alcanza para la siguiente, se detiene
     * - NO se permiten pagos con excedentes (monto exacto requerido)
     * - No se paga por ID, sino por orden y monto, como lo especifica el enunciado de la prueba técnica
     *
     * @param monto Amount to pay
     * @return PaymentResult containing information about the payment process
     */
    @Transactional
    public PaymentResult pagar(BigDecimal monto) {
        List<Transaccion> pendientes = transaccionRepository.findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE);
        
        if (pendientes.isEmpty()) {
            return new PaymentResult(0, monto, monto, null);
        }
        
        BigDecimal montoInicial = monto;
        int transaccionesPagadas = 0;
        BigDecimal montoRequerido = null;
        
        // Calcular el monto total requerido para las transacciones que se pueden pagar completamente
        BigDecimal montoTotalRequerido = BigDecimal.ZERO;
        int transaccionesQueSePuedenPagar = 0;
        
        for (Transaccion transaccion : pendientes) {
            BigDecimal montoNecesario = montoTotalRequerido.add(transaccion.getValor());
            if (montoNecesario.compareTo(monto) <= 0) {
                montoTotalRequerido = montoNecesario;
                transaccionesQueSePuedenPagar++;
            } else {
                break;
            }
        }
        
        // Si no hay transacciones que se puedan pagar completamente, el monto es insuficiente
        if (transaccionesQueSePuedenPagar == 0) {
            montoRequerido = pendientes.get(0).getValor();
            return new PaymentResult(0, monto, montoInicial, montoRequerido);
        }
        
        // Si hay transacciones que se pueden pagar pero el monto excede el total requerido, rechazar el pago
        if (monto.compareTo(montoTotalRequerido) > 0) {
            montoRequerido = montoTotalRequerido;
            return new PaymentResult(0, monto, montoInicial, montoRequerido);
        }
        
        // Si el monto es exacto o insuficiente, proceder con el pago
        for (Transaccion transaccion : pendientes) {
            if (monto.compareTo(transaccion.getValor()) >= 0) {
                // Pago exitoso
                transaccion.setEstado(EstadoTransaccion.PAGADO);
                transaccionRepository.save(transaccion);
                transaccionesPagadas++;
                monto = monto.subtract(transaccion.getValor());
            } else {
                // Monto insuficiente, capturar el monto requerido
                montoRequerido = transaccion.getValor();
                break;
            }
        }
        
        return new PaymentResult(transaccionesPagadas, monto, montoInicial, montoRequerido);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id Transaction ID
     * @return Optional containing the transaction as DTO if found, empty otherwise
     */
    public Optional<TransaccionDTO> obtenerPorId(Long id) {
        return transaccionRepository.findById(id).map(this::toDTO);
    }

    /**
     * Converts a Transaccion entity to its DTO representation.
     *
     * @param t Transaccion entity
     * @return TransaccionDTO
     */
    private TransaccionDTO toDTO(Transaccion t) {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(t.getId());
        dto.setNombre(t.getNombre());
        dto.setFecha(t.getFecha());
        dto.setValor(t.getValor());
        dto.setEstado(t.getEstado());
        return dto;
    }
}

