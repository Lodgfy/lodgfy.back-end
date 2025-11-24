package sptech.school.Lodgfy.business.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChaleManager (Subject) - Gerencia observers e notifica mudanças nos chalés.
 * Utiliza CopyOnWriteArrayList para thread-safety.
 */
@Slf4j
@Component
public class ChaleManager {

    // Lista thread-safe de observers
    private final List<ChaleObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Registra um observer para receber notificações de mudanças.
     *
     * @param observer O observer a ser registrado
     */
    public void registrar(ChaleObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            log.info("Observer registrado: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Remove um observer da lista de notificações.
     *
     * @param observer O observer a ser removido
     */
    public void remover(ChaleObserver observer) {
        if (observers.remove(observer)) {
            log.info("Observer removido: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Notifica todos os observers registrados sobre uma mudança no chalé.
     *
     * @param chale O chalé que foi modificado
     * @param eventoTipo O tipo de evento que ocorreu
     */
    public void notificar(ChaleEntity chale, ChaleObserver.ChaleEventType eventoTipo) {
        log.debug("Notificando {} observers sobre evento {} para chalé ID: {}",
                  observers.size(), eventoTipo, chale.getIdChale());

        for (ChaleObserver observer : observers) {
            try {
                observer.onChaleChanged(chale, eventoTipo);
            } catch (Exception e) {
                log.error("Erro ao notificar observer {}: {}",
                         observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Retorna o número de observers registrados.
     *
     * @return quantidade de observers
     */
    public int getObserverCount() {
        return observers.size();
    }

    /**
     * Remove todos os observers.
     */
    public void limparObservers() {
        observers.clear();
        log.info("Todos os observers foram removidos");
    }
}

