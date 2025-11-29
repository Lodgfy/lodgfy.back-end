package sptech.school.Lodgfy.business.observer.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sptech.school.Lodgfy.business.observer.ChaleObserver;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer que mantém histórico de mudanças em chalés.
 * Exemplo de implementação que poderia salvar em banco de dados
 * ou enviar para sistema de auditoria externo.
 */
@Slf4j
@Component
public class AuditoriaChaleObserver implements ChaleObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void onChaleChanged(ChaleEntity chale, ChaleObserver.ChaleEventType eventoTipo) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String mensagemAuditoria = construirMensagemAuditoria(chale, eventoTipo, timestamp);

        // Em produção, salvaria no banco de dados ou enviaria para sistema de auditoria
        log.info("📝 [AUDITORIA] {}", mensagemAuditoria);

        // Exemplo de estrutura que poderia ser salva:
        // AuditoriaEntity auditoria = new AuditoriaEntity();
        // auditoria.setEntidade("CHALE");
        // auditoria.setEntidadeId(chale.getIdChale());
        // auditoria.setTipoEvento(eventoTipo.name());
        // auditoria.setDataHora(LocalDateTime.now());
        // auditoria.setDetalhes(mensagemAuditoria);
        // auditoriaRepository.save(auditoria);
    }

    private String construirMensagemAuditoria(ChaleEntity chale, ChaleObserver.ChaleEventType tipo, String timestamp) {
        return switch (tipo) {
            case CRIADO -> String.format(
                "[%s] Novo chalé criado - ID: %d, Nome: '%s', Número: '%s', Status: %s, Valor: R$ %s",
                timestamp, chale.getIdChale(), chale.getNome(), chale.getNumero(),
                chale.getStatus(), chale.getValorDiaria()
            );

            case ATUALIZADO -> String.format(
                "[%s] Chalé atualizado - ID: %d, Nome: '%s'",
                timestamp, chale.getIdChale(), chale.getNome()
            );

            case REMOVIDO -> String.format(
                "[%s] Chalé removido - ID: %d, Nome: '%s', Número: '%s'",
                timestamp, chale.getIdChale(), chale.getNome(), chale.getNumero()
            );

            case STATUS_ALTERADO -> String.format(
                "[%s] Status alterado - Chalé ID: %d ('%s') -> Novo status: %s",
                timestamp, chale.getIdChale(), chale.getNome(), chale.getStatus()
            );

            case PRECO_ALTERADO -> String.format(
                "[%s] Preço alterado - Chalé ID: %d ('%s') -> Novo valor: R$ %s",
                timestamp, chale.getIdChale(), chale.getNome(), chale.getValorDiaria()
            );
        };
    }
}
