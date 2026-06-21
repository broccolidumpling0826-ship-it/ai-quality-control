package com.jhict.quality.gateway;

import com.jhict.quality.gateway.model.ModelGateway;
import com.jhict.quality.gateway.vector.VectorStoreGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Logs sanitized AI gateway health at startup.
 */
@Slf4j
@Component
public class AiGatewayHealthLogger {

    private final List<ModelGateway> modelGateways;

    private final List<VectorStoreGateway> vectorStoreGateways;

    @Autowired
    public AiGatewayHealthLogger(List<ModelGateway> modelGateways, List<VectorStoreGateway> vectorStoreGateways) {
        this.modelGateways = modelGateways == null ? Collections.emptyList() : modelGateways;
        this.vectorStoreGateways = vectorStoreGateways == null ? Collections.emptyList() : vectorStoreGateways;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logGatewayHealth() {
        for (ModelGateway gateway : modelGateways) {
            log.info("AI模型网关状态，provider={}, enabled={}", gateway.provider(), gateway.enabled());
        }
        for (VectorStoreGateway gateway : vectorStoreGateways) {
            log.info("AI向量网关状态，provider={}, enabled={}", gateway.provider(), gateway.enabled());
        }
    }
}
