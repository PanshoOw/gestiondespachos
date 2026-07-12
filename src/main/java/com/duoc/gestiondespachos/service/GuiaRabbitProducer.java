package com.duoc.gestiondespachos.service;

import com.duoc.gestiondespachos.config.RabbitMqConfig;
import com.duoc.gestiondespachos.dto.GuiaDespachoMensajeDTO;
import com.duoc.gestiondespachos.entity.GuiaDespacho;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class GuiaRabbitProducer {

    private final AmqpTemplate amqpTemplate;

    public GuiaRabbitProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void enviarGuia(GuiaDespacho guia) {

        GuiaDespachoMensajeDTO mensaje = new GuiaDespachoMensajeDTO(
                guia.getId(),
                guia.getNumeroPedido(),
                guia.getTransportista(),
                guia.getDestinatario(),
                guia.getDireccionDestino(),
                guia.getCiudadDestino(),
                guia.getFechaDespacho(),
                guia.getDescripcionCarga(),
                guia.getPesoKg(),
                guia.getEstado().name(),
                guia.getFechaCreacion()
        );

        amqpTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_GUIAS,
                RabbitMqConfig.ROUTING_GUIAS,
                mensaje
        );
    }
}