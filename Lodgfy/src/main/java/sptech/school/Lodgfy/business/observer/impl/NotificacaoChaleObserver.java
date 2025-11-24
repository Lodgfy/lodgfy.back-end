package sptech.school.Lodgfy.business.observer.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sptech.school.Lodgfy.business.observer.ChaleObserver;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

/**
 * Observer concreto que simula o envio de notificações quando um chalé é modificado.
 * Em produção, poderia enviar emails, SMS, push notifications, etc.
 */
@Slf4j
@Component
public class NotificacaoChaleObserver implements ChaleObserver {

    @Override
    public void onChaleChanged(ChaleEntity chale, ChaleObserver.ChaleEventType eventoTipo) {
        switch (eventoTipo) {
            case STATUS_ALTERADO:
                notificarMudancaStatus(chale);
                break;
            case DISPONIBILIDADE_ALTERADA:
                notificarMudancaDisponibilidade(chale);
                break;
            case PRECO_ALTERADO:
                notificarMudancaPreco(chale);
                break;
            case CRIADO:
                notificarNovoChaleCriado(chale);
                break;
            case ATUALIZADO:
                notificarChaleAtualizado(chale);
                break;
            case REMOVIDO:
                notificarChaleRemovido(chale);
                break;
        }
    }

    private void notificarMudancaStatus(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Status do chalé '{}' alterado para {}",
                chale.getNome(), chale.getStatus());
        // Implementar lógica de envio de notificação (email, SMS, etc)
    }

    private void notificarMudancaDisponibilidade(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Disponibilidade do chalé '{}' alterada para {}",
                chale.getNome(), chale.getDisponivel() ? "DISPONÍVEL" : "INDISPONÍVEL");
        // Implementar lógica de envio de notificação
    }

    private void notificarMudancaPreco(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Preço do chalé '{}' alterado para R$ {}",
                chale.getNome(), chale.getValorDiaria());
        // Implementar lógica de envio de notificação
    }

    private void notificarNovoChaleCriado(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Novo chalé '{}' cadastrado no sistema",
                chale.getNome());
        // Implementar lógica de envio de notificação
    }

    private void notificarChaleAtualizado(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Chalé '{}' foi atualizado",
                chale.getNome());
        // Implementar lógica de envio de notificação
    }

    private void notificarChaleRemovido(ChaleEntity chale) {
        log.info("📧 Enviando notificação: Chalé '{}' foi removido do sistema",
                chale.getNome());
        // Implementar lógica de envio de notificação
    }
}
