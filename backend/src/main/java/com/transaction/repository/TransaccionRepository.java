package com.transaction.repository;

import com.transaction.entity.EstadoTransaccion;
import com.transaction.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for accessing transaction data in the database.
 * <p>
 * Extends JpaRepository to provide CRUD operations and custom queries for filtering and payment logic.
 */
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    /**
     * Finds transactions by optional filters: name (partial, case-insensitive), date, and state.
     *
     * @param nombre Name filter (nullable, partial match)
     * @param fecha  Date filter (nullable, exact match)
     * @param estado State filter (nullable, exact match)
     * @return List of transactions matching the filters
     */
    @Query("SELECT t FROM Transaccion t WHERE (:nombre IS NULL OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND (:fecha IS NULL OR t.fecha = :fecha) AND (:estado IS NULL OR t.estado = :estado)")
    List<Transaccion> findByFilters(@Param("nombre") String nombre, @Param("fecha") LocalDate fecha, @Param("estado") EstadoTransaccion estado);

    /**
     * Finds all transactions with the given state, ordered by date ascending.
     * Used for payment processing (oldest first).
     *
     * @param estado State to filter by (e.g., PENDIENTE)
     * @return List of transactions in the given state, ordered by date
     */
    List<Transaccion> findByEstadoOrderByFechaAsc(EstadoTransaccion estado);
}