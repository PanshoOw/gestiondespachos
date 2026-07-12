package com.duoc.gestiondespachos.service;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

import com.duoc.gestiondespachos.config.RabbitMqConfig;
import com.duoc.gestiondespachos.dto.ConsumoColaResponseDTO;
import com.duoc.gestiondespachos.dto.GuiaDespachoMensajeDTO;
import com.duoc.gestiondespachos.entity.GuiaProcesadaCola;
import com.duoc.gestiondespachos.repository.GuiaProcesadaColaRepository;

@Service
public class GuiaRabbitConsumerService {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;
    private final GuiaProcesadaColaRepository guiaProcesadaColaRepository;

    public GuiaRabbitConsumerService(
            RabbitTemplate rabbitTemplate,
            MessageConverter messageConverter,
            GuiaProcesadaColaRepository guiaProcesadaColaRepository) {

        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
        this.guiaProcesadaColaRepository = guiaProcesadaColaRepository;
    }

    public ConsumoColaResponseDTO consumirMensajes() {

        int mensajesRecibidos = 0;
        int mensajesGuardados = 0;
        int mensajesEnviadosAErrores = 0;

        Message mensajeRabbit;

        while ((mensajeRabbit =
                rabbitTemplate.receive(RabbitMqConfig.COLA_GUIAS)) != null) {

            mensajesRecibidos++;

            try {
                Object objetoConvertido =
                        messageConverter.fromMessage(mensajeRabbit);

                if (!(objetoConvertido
                        instanceof GuiaDespachoMensajeDTO mensajeGuia)) {

                    throw new IllegalArgumentException(
                            "El mensaje no corresponde a una guía válida."
                    );
                }

                String payloadJson = new String(
                        mensajeRabbit.getBody(),
                        StandardCharsets.UTF_8
                );

                GuiaProcesadaCola guiaProcesada = new GuiaProcesadaCola();

                guiaProcesada.setIdGuiaOriginal(mensajeGuia.id());
                guiaProcesada.setNumeroPedido(mensajeGuia.numeroPedido());
                guiaProcesada.setTransportista(mensajeGuia.transportista());
                guiaProcesada.setFechaDespacho(mensajeGuia.fechaDespacho());
                guiaProcesada.setPayloadJson(payloadJson);

                guiaProcesadaColaRepository.saveAndFlush(guiaProcesada);

                mensajesGuardados++;

            } catch (Exception exception) {

                enviarAColaErrores(mensajeRabbit, exception);
                mensajesEnviadosAErrores++;
            }
        }

        String detalle = mensajesRecibidos == 0
                ? "No había mensajes disponibles en la cola principal."
                : "Consumo de la cola finalizado.";

        return new ConsumoColaResponseDTO(
                mensajesRecibidos,
                mensajesGuardados,
                mensajesEnviadosAErrores,
                detalle
        );
    }

    private void enviarAColaErrores(
            Message mensajeOriginal,
            Exception exception) {

        String detalleError = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();

        Message mensajeError = MessageBuilder
                .withBody(mensajeOriginal.getBody())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("x-error-message", detalleError)
                .setHeader(
                        "x-original-queue",
                        RabbitMqConfig.COLA_GUIAS
                )
                .build();

        rabbitTemplate.send(
                RabbitMqConfig.EXCHANGE_ERRORES,
                RabbitMqConfig.ROUTING_ERRORES,
                mensajeError
        );
    }
}