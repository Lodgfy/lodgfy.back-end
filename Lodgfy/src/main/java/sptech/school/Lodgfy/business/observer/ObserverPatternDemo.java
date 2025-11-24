package sptech.school.Lodgfy.business.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sptech.school.Lodgfy.business.ChaleService;
import sptech.school.Lodgfy.business.dto.ChaleRequestDTO;
import sptech.school.Lodgfy.business.dto.ChaleResponseDTO;
import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

import java.math.BigDecimal;

/**
 * Exemplo de demonstração do padrão Observer em ação.
 * Execute a aplicação com o perfil 'demo' para ver as notificações:
 * mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=demo
 */
@Slf4j
@Component
@Profile("demo")
public class ObserverPatternDemo implements CommandLineRunner {

    private final ChaleService chaleService;

    public ObserverPatternDemo(ChaleService chaleService) {
        this.chaleService = chaleService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("╔═══════════════════════════════════════════════════╗");
        log.info("║   DEMONSTRAÇÃO DO PADRÃO OBSERVER - CHALÉS       ║");
        log.info("╚═══════════════════════════════════════════════════╝");

        Thread.sleep(1000);

        // 1. Criar novo chalé (dispara evento CRIADO)
        log.info("\n>>> 1. Criando novo chalé...");
        ChaleRequestDTO novoChale = new ChaleRequestDTO();
        novoChale.setNome("Chalé Vista Mar");
        novoChale.setNumero("101");
        novoChale.setTipo("Luxo");
        novoChale.setValorDiaria(new BigDecimal("350.00"));
        novoChale.setDisponivel(true);
        novoChale.setCapacidade(4);
        novoChale.setDescricao("Chalé luxuoso com vista para o mar");
        novoChale.setStatus(ChaleEntity.StatusChale.DISPONIVEL);

        ChaleResponseDTO chaleCriado = chaleService.salvarChale(novoChale);
        Thread.sleep(2000);

        // 2. Atualizar status (dispara evento STATUS_ALTERADO)
        log.info("\n>>> 2. Alterando status do chalé para OCUPADO...");
        chaleService.atualizarStatus(chaleCriado.getIdChale(), ChaleEntity.StatusChale.OCUPADO);
        Thread.sleep(2000);

        // 3. Alterar disponibilidade (dispara evento DISPONIBILIDADE_ALTERADA)
        log.info("\n>>> 3. Tornando chalé indisponível...");
        chaleService.atualizarDisponibilidade(chaleCriado.getIdChale(), false);
        Thread.sleep(2000);

        // 4. Atualizar dados gerais (dispara evento ATUALIZADO/PRECO_ALTERADO)
        log.info("\n>>> 4. Atualizando preço do chalé...");
        ChaleRequestDTO chaleAtualizado = new ChaleRequestDTO();
        chaleAtualizado.setNome("Chalé Vista Mar Premium");
        chaleAtualizado.setNumero("101");
        chaleAtualizado.setTipo("Luxo Premium");
        chaleAtualizado.setValorDiaria(new BigDecimal("450.00")); // Preço alterado
        chaleAtualizado.setDisponivel(false);
        chaleAtualizado.setCapacidade(4);
        chaleAtualizado.setDescricao("Chalé luxuoso com vista para o mar - Reformado");
        chaleAtualizado.setStatus(ChaleEntity.StatusChale.OCUPADO);

        chaleService.atualizarChale(chaleCriado.getIdChale(), chaleAtualizado);
        Thread.sleep(2000);

        // 5. Alterar para manutenção
        log.info("\n>>> 5. Colocando chalé em manutenção...");
        chaleService.atualizarStatus(chaleCriado.getIdChale(), ChaleEntity.StatusChale.MANUTENCAO);
        Thread.sleep(2000);

        log.info("\n╔═══════════════════════════════════════════════════╗");
        log.info("║   DEMONSTRAÇÃO CONCLUÍDA                          ║");
        log.info("║   Verifique os logs acima para ver as            ║");
        log.info("║   notificações dos Observers em ação!            ║");
        log.info("╚═══════════════════════════════════════════════════╝\n");
    }
}
