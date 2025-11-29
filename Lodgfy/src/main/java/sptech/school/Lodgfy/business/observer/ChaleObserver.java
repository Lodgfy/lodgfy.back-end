package sptech.school.Lodgfy.business.observer;

import sptech.school.Lodgfy.infrastructure.entities.ChaleEntity;

public interface ChaleObserver {
    
    void onChaleChanged(ChaleEntity chale, ChaleEventType eventoTipo);

    enum ChaleEventType {
        STATUS_ALTERADO,
        PRECO_ALTERADO,
        CRIADO,
        ATUALIZADO,
        REMOVIDO
    }
}
