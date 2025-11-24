package sptech.school.Lodgfy.business.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import sptech.school.Lodgfy.business.observer.ChaleManager;
import sptech.school.Lodgfy.business.observer.impl.AuditoriaChaleObserver;
import sptech.school.Lodgfy.business.observer.impl.LogChaleObserver;
import sptech.school.Lodgfy.business.observer.impl.NotificacaoChaleObserver;

/**
 * Configuração do padrão Observer clássico para Chalés.
 * Registra automaticamente os observers no ChaleManager ao iniciar a aplicação.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ObserverConfig {

    private final ChaleManager chaleManager;
    private final LogChaleObserver logChaleObserver;
    private final NotificacaoChaleObserver notificacaoChaleObserver;
    private final AuditoriaChaleObserver auditoriaChaleObserver;

    /**
     * Registra todos os observers automaticamente após a construção do bean.
     */
    @PostConstruct
    public void registrarObservers() {
        log.info("Registrando observers de Chalé...");

        chaleManager.registrar(logChaleObserver);
        chaleManager.registrar(notificacaoChaleObserver);
        chaleManager.registrar(auditoriaChaleObserver);

        log.info("Total de observers registrados: {}", chaleManager.getObserverCount());
    }
}
