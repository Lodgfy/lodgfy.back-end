package sptech.school.Lodgfy.business.observer.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sptech.school.Lodgfy.business.observer.ChaleObserver;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

/**
 * Observer concreto que registra logs quando um chalé é modificado.
 */
@Slf4j
@Component
public class LogChaleObserver implements ChaleObserver {

    @Override
    public void onChaleChanged(ChaleEntity chale, ChaleObserver.ChaleEventType eventoTipo) {
        log.info("=== NOTIFICAÇÃO DE MUDANÇA NO CHALÉ ===");
        log.info("Evento: {}", eventoTipo);
        log.info("Chalé ID: {}", chale.getIdChale());
        log.info("Nome: {}", chale.getNome());
        log.info("Número: {}", chale.getNumero());
        log.info("Status: {}", chale.getStatus());
        log.info("Valor Diária: {}", chale.getValorDiaria());
        log.info("=====================================");
    }
}
