package com.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.dto.TransaccionCreateDTO;
import com.transaction.entity.EstadoTransaccion;
import com.transaction.entity.Transaccion;
import com.transaction.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransaccionController.
 * Tests the complete request-response cycle including database operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransaccionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transaccionRepository.deleteAll();
    }

    @Test
    void testCrearTransaccion() throws Exception {
        // Given
        TransaccionCreateDTO dto = new TransaccionCreateDTO();
        dto.setNombre("Test Transaction");
        dto.setFecha(LocalDate.of(2024, 1, 1));
        dto.setValor(new BigDecimal("100.00"));

        // When & Then
        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test Transaction"))
                .andExpect(jsonPath("$.fecha").value("2024-01-01"))
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void testCrearTransaccion_ValidacionError() throws Exception {
        // Given
        TransaccionCreateDTO dto = new TransaccionCreateDTO();
        dto.setNombre(""); // Nombre vacío
        dto.setFecha(LocalDate.of(2024, 1, 1));
        dto.setValor(new BigDecimal("-10.00")); // Valor negativo

        // When & Then
        mockMvc.perform(post("/api/transacciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListarTransacciones() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Test Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        transaccionRepository.save(transaccion);

        // When & Then
        mockMvc.perform(get("/api/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Test Transaction"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void testListarTransaccionesConFiltros() throws Exception {
        // Given
        Transaccion transaccion1 = Transaccion.builder()
                .nombre("Test Transaction 1")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        Transaccion transaccion2 = Transaccion.builder()
                .nombre("Test Transaction 2")
                .fecha(LocalDate.of(2024, 1, 2))
                .valor(new BigDecimal("200.00"))
                .estado(EstadoTransaccion.PAGADO)
                .build();
        transaccionRepository.save(transaccion1);
        transaccionRepository.save(transaccion2);

        // When & Then
        mockMvc.perform(get("/api/transacciones")
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void testObtenerTransaccionPorId() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Test Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        Transaccion saved = transaccionRepository.save(transaccion);

        // When & Then
        mockMvc.perform(get("/api/transacciones/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.nombre").value("Test Transaction"));
    }

    @Test
    void testObtenerTransaccionPorId_NoExiste() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/transacciones/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEditarTransaccionPendiente() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Original Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        Transaccion saved = transaccionRepository.save(transaccion);

        TransaccionCreateDTO dto = new TransaccionCreateDTO();
        dto.setNombre("Updated Transaction");
        dto.setFecha(LocalDate.of(2024, 1, 2));
        dto.setValor(new BigDecimal("150.00"));

        // When & Then
        mockMvc.perform(put("/api/transacciones/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Updated Transaction"))
                .andExpect(jsonPath("$.valor").value(150.00));
    }

    @Test
    void testEditarTransaccionPagada_Error() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Paid Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PAGADO)
                .build();
        Transaccion saved = transaccionRepository.save(transaccion);

        TransaccionCreateDTO dto = new TransaccionCreateDTO();
        dto.setNombre("Updated Transaction");
        dto.setFecha(LocalDate.of(2024, 1, 2));
        dto.setValor(new BigDecimal("150.00"));

        // When & Then
        mockMvc.perform(put("/api/transacciones/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testEliminarTransaccionPendiente() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Test Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        Transaccion saved = transaccionRepository.save(transaccion);

        // When & Then
        mockMvc.perform(delete("/api/transacciones/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Verify it was deleted
        mockMvc.perform(get("/api/transacciones/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarTransaccionPagada_Error() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Paid Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PAGADO)
                .build();
        Transaccion saved = transaccionRepository.save(transaccion);

        // When & Then
        mockMvc.perform(delete("/api/transacciones/" + saved.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void testRealizarPago() throws Exception {
        // Given
        Transaccion transaccion1 = Transaccion.builder()
                .nombre("Transaction 1")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        Transaccion transaccion2 = Transaccion.builder()
                .nombre("Transaction 2")
                .fecha(LocalDate.of(2024, 1, 2))
                .valor(new BigDecimal("200.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        transaccionRepository.save(transaccion1);
        transaccionRepository.save(transaccion2);

        // When & Then - 150.00 excede el monto exacto requerido de 100.00
        mockMvc.perform(post("/api/transacciones/pagar")
                        .param("monto", "150.00"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pago rechazado. El monto $150.00 excede el monto exacto requerido de $100.00")));
    }

    @Test
    void testRealizarPago_TransaccionParcial() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        transaccionRepository.save(transaccion);

        // When & Then - 50.00 es insuficiente para pagar 100.00
        mockMvc.perform(post("/api/transacciones/pagar")
                        .param("monto", "50.00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Monto insuficiente")));
    }

    @Test
    void testRealizarPago_MontoExacto() throws Exception {
        // Given
        Transaccion transaccion = Transaccion.builder()
                .nombre("Transaction")
                .fecha(LocalDate.of(2024, 1, 1))
                .valor(new BigDecimal("100.00"))
                .estado(EstadoTransaccion.PENDIENTE)
                .build();
        transaccionRepository.save(transaccion);

        // When & Then - 100.00 es exacto para pagar 100.00
        mockMvc.perform(post("/api/transacciones/pagar")
                        .param("monto", "100.00"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pago exitoso")));
    }
} 