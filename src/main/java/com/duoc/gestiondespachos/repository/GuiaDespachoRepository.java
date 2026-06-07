package com.duoc.gestiondespachos.repository;

import com.duoc.gestiondespachos.entity.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {

    Optional<GuiaDespacho> findByNumeroPedidoIgnoreCase(String numeroPedido);

    boolean existsByNumeroPedidoIgnoreCase(String numeroPedido);

    List<GuiaDespacho> findByTransportistaIgnoreCaseAndFechaDespacho(String transportista, LocalDate fechaDespacho);

    List<GuiaDespacho> findByTransportistaIgnoreCase(String transportista);

    List<GuiaDespacho> findByFechaDespacho(LocalDate fechaDespacho);
}