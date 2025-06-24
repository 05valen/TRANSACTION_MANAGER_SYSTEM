package com.transaction.service;

import com.transaction.dto.TransaccionCreateDTO;
import com.transaction.dto.TransaccionDTO;
import com.transaction.entity.EstadoTransaccion;
import com.transaction.entity.Transaccion;
import com.transaction.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransaccionService.
 * Tests business logic and service layer functionality.
 */
@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private Transaccion transaccionPendiente;
    private Transaccion transaccionPagada;
    private TransaccionCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        transaccionPendiente = Transaccion.builder()
                .id(1L)
                .nombre("Test Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();

        transaccionPagada = Transaccion.builder()
                .id(2L)
                .nombre("Paid Transaction")
                .fecha(LocalDate.of(2024, 1, 2))
                .valor(new BigDecimal("200.00"))
                .estado(EstadoTransaccion.PAGADO)
                .build();

        createDTO = new TransaccionCreateDTO();
        createDTO.setNombre("New Transaction");
        createDTO.setFecha(LocalDate.of(2024, 1, 3));
        createDTO.setValor(new BigDecimal("150.00"));
    }

    @Test
    void testCrearTransaccion() {
        // Given
        Transaccion transaccionCreada = Transaccion.builder()
                .id(1L)
                .nombre(createDTO.getNombre())
                .fecha(createDTO.getFecha())
                .valor(createDTO.getValor())
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionCreada);

        // When
        TransaccionDTO result = transaccionService.crear(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(createDTO.getNombre(), result.getNombre());
        assertEquals(createDTO.getFecha(), result.getFecha());
        assertEquals(createDTO.getValor(), result.getValor());
        assertEquals(EstadoTransaccion.PENDIENTE, result.getEstado());
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    void testListarConFiltros() {
        // Given
        List<Transaccion> transacciones = Arrays.asList(transaccionPendiente, transaccionPagada);
        when(transaccionRepository.findByFilters("Test", LocalDate.of(2024, 1, 1), EstadoTransaccion.PENDIENTE))
                .thenReturn(transacciones);

        // When
        List<TransaccionDTO> result = transaccionService.listar("Test", LocalDate.of(2024, 1, 1), EstadoTransaccion.PENDIENTE);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transaccionRepository).findByFilters("Test", LocalDate.of(2024, 1, 1), EstadoTransaccion.PENDIENTE);
    }

    @Test
    void testEditarTransaccionPendiente() {
        // Given
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccionPendiente));
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionPendiente);

        // When
        TransaccionDTO result = transaccionService.editar(1L, createDTO);

        // Then
        assertNotNull(result);
        verify(transaccionRepository).findById(1L);
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    void testEditarTransaccionPagada_ThrowsException() {
        // Given
        when(transaccionRepository.findById(2L)).thenReturn(Optional.of(transaccionPagada));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transaccionService.editar(2L, createDTO);
        });
        verify(transaccionRepository).findById(2L);
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void testEliminarTransaccionPendiente() {
        // Given
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccionPendiente));

        // When
        transaccionService.eliminar(1L);

        // Then
        verify(transaccionRepository).findById(1L);
        verify(transaccionRepository).deleteById(1L);
    }

    @Test
    void testEliminarTransaccionPagada_ThrowsException() {
        // Given
        when(transaccionRepository.findById(2L)).thenReturn(Optional.of(transaccionPagada));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transaccionService.eliminar(2L);
        });
        verify(transaccionRepository).findById(2L);
        verify(transaccionRepository, never()).deleteById(anyLong());
    }

    @Test
    void testPagar_TransaccionesCompletas() {
        // Given
        List<Transaccion> pendientes = Arrays.asList(transaccionPendiente);
        when(transaccionRepository.findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE))
                .thenReturn(pendientes);

        // When - Intentar pagar 150.00 cuando solo necesitamos 100.00 (excedente)
        PaymentResult resultado = transaccionService.pagar(new BigDecimal("150.00"));

        // Then - No se debe pagar nada porque hay excedente
        assertEquals(new BigDecimal("150.00"), resultado.getMontoRestante());
        assertEquals(0, resultado.getTransaccionesPagadas());
        verify(transaccionRepository).findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE);
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void testPagar_TransaccionParcial_NoPaga() {
        // Given
        List<Transaccion> pendientes = Arrays.asList(transaccionPendiente);
        when(transaccionRepository.findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE))
                .thenReturn(pendientes);

        // When - Intentar pagar 50.00 cuando necesitamos 100.00 (insuficiente)
        PaymentResult resultado = transaccionService.pagar(new BigDecimal("50.00"));

        // Then - No se debe pagar nada porque el monto es insuficiente
        assertEquals(new BigDecimal("50.00"), resultado.getMontoRestante());
        assertEquals(0, resultado.getTransaccionesPagadas());
        verify(transaccionRepository).findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE);
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void testPagar_MontoExacto_PagaCorrectamente() {
        // Given
        List<Transaccion> pendientes = Arrays.asList(transaccionPendiente);
        when(transaccionRepository.findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE))
                .thenReturn(pendientes);
        when(transaccionRepository.save(any(Transaccion.class))).thenReturn(transaccionPendiente);

        // When - Pagar exactamente 100.00 para una transacción de 100.00
        PaymentResult resultado = transaccionService.pagar(new BigDecimal("100.00"));

        // Then - Se debe pagar correctamente
        assertEquals(new BigDecimal("0.00"), resultado.getMontoRestante());
        assertEquals(1, resultado.getTransaccionesPagadas());
        verify(transaccionRepository).findByEstadoOrderByFechaAsc(EstadoTransaccion.PENDIENTE);
        verify(transaccionRepository).save(any(Transaccion.class));
    }

    @Test
    void testObtenerPorId_Existe() {
        // Given
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccionPendiente));

        // When
        Optional<TransaccionDTO> result = transaccionService.obtenerPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(transaccionRepository).findById(1L);
    }

    @Test
    void testObtenerPorId_NoExiste() {
        // Given
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TransaccionDTO> result = transaccionService.obtenerPorId(999L);

        // Then
        assertFalse(result.isPresent());
        verify(transaccionRepository).findById(999L);
    }
} 