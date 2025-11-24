package sptech.school.Lodgfy.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sptech.school.Lodgfy.business.dto.OrcamentoEventoDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * Implementação usando JavaMailSender para enviar e-mails em HTML.
 */
@Service
public class EmailService {

    @Value("${lodgfy.email.orcamento:guimontinalves@gmail.com}")
    private String emailOrcamento;

    @Autowired
    private JavaMailSender mailSender;

    public void enviarOrcamentoEvento(OrcamentoEventoDTO orcamento) {
        try {
            // Envia para equipe
            MimeMessage messageEquipe = mailSender.createMimeMessage();
            MimeMessageHelper helperEquipe = new MimeMessageHelper(messageEquipe, true, "UTF-8");
            helperEquipe.setTo(emailOrcamento);
            helperEquipe.setSubject("[Lodgfy] Novo orçamento: " + orcamento.getTipoEvento());
            helperEquipe.setText(construirHtmlEquipe(orcamento), true);
            mailSender.send(messageEquipe);

            // Envia para cliente
            MimeMessage messageCliente = mailSender.createMimeMessage();
            MimeMessageHelper helperCliente = new MimeMessageHelper(messageCliente, true, "UTF-8");
            helperCliente.setTo(orcamento.getEmail());
            helperCliente.setSubject("Recebemos sua solicitação de orçamento - Lodgfy");
            helperCliente.setText(construirHtmlCliente(orcamento), true);
            mailSender.send(messageCliente);

        } catch (MessagingException e) {
            System.err.println("Erro ao processar envio de e-mail: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }

    private String construirHtmlEquipe(OrcamentoEventoDTO o) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
                "<body style=\"font-family:Arial,Helvetica,sans-serif;color:#333;margin:0;padding:0;background:#f4f4f6;\">" +
                "<div style=\"max-width:680px;margin:32px auto;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 4px 18px rgba(0,0,0,0.06);\">" +
                "<div style=\"background:#111827;color:#ffd54a;padding:18px 24px;\">" +
                "<h2 style=\"margin:0;font-size:18px;\">Novo orçamento de evento</h2>" +
                "</div>" +
                "<div style=\"padding:20px 24px;\">" +
                "<p style=\"margin:0 0 12px 0;\"><strong>Tipo:</strong> " + escapeHtml(o.getTipoEvento()) + "</p>" +
                "<p style=\"margin:0 0 12px 0;\"><strong>Nome:</strong> " + escapeHtml(o.getNome()) + "</p>" +
                "<p style=\"margin:0 0 12px 0;\"><strong>Data:</strong> " + o.getData().format(f) + "</p>" +
                "<p style=\"margin:0 0 12px 0;\"><strong>E-mail:</strong> " + escapeHtml(o.getEmail()) + "</p>" +
                "<p style=\"margin:0 0 12px 0;\"><strong>Convidados:</strong> " + o.getConvidados() + "</p>" +
                "<hr style=\"border:none;border-top:1px solid #eee;margin:18px 0;\">" +
                "<p style=\"margin:0 0 8px 0;\"><strong>Ações sugeridas:</strong></p>" +
                "<ul style=\"margin:0 0 16px 20px;color:#444;\">" +
                "<li>Verificar disponibilidade do espaço na data solicitada</li>" +
                "<li>Preparar opções de pacotes e custos</li>" +
                "<li>Entrar em contato com o cliente em até 24 horas</li>" +
                "</ul>" +
                "</div>" +
                "<div style=\"background:#f7f7f8;padding:12px 24px;color:#666;font-size:13px;\">" +
                "Equipe Lodgfy - Sistema de Orçamentos" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String construirHtmlCliente(OrcamentoEventoDTO o) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
                "<body style=\"font-family:Arial,Helvetica,sans-serif;color:#333;margin:0;padding:0;background:#f4f4f6;\">" +
                "<div style=\"max-width:680px;margin:32px auto;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 4px 18px rgba(0,0,0,0.06);\">" +
                "<div style=\"background:#111827;color:#ffd54a;padding:22px 24px;\">" +
                "<h1 style=\"margin:0;font-size:20px;\">Solicitação de Orçamento Recebida</h1>" +
                "</div>" +
                "<div style=\"padding:22px 24px;line-height:1.6;\">" +
                "<p>Olá <strong>" + escapeHtml(o.getNome()) + "</strong>,</p>" +
                "<p>Obrigado por solicitar um orçamento com a <strong>Lodgfy</strong>. Recebemos sua solicitação e estamos preparando uma proposta personalizada.</p>" +
                "<div style=\"background:#f9fafb;border-left:4px solid #111827;padding:12px 14px;margin:16px 0;border-radius:4px;\">" +
                "<p style=\"margin:0 0 8px 0;\"><strong>Resumo da solicitação</strong></p>" +
                "<p style=\"margin:0;\"><strong>Tipo de evento:</strong> " + escapeHtml(o.getTipoEvento()) + "</p>" +
                "<p style=\"margin:0;\"><strong>Data:</strong> " + o.getData().format(f) + "</p>" +
                "<p style=\"margin:0;\"><strong>Convidados:</strong> " + o.getConvidados() + "</p>" +
                "</div>" +
                "<p>Nossa equipe entrará em contato em até <strong>24 horas</strong> com as opções disponíveis e valores.</p>" +
                "<p>Enquanto isso, se desejar, responda este e-mail com informações adicionais (ex.: preferências de buffet, necessidade de hospedagem, itens extras).</p>" +
                "<p>Atenciosamente,<br><strong>Equipe Lodgfy</strong></p>" +
                "</div>" +
                "<div style=\"background:#f7f7f8;padding:12px 24px;color:#666;font-size:13px;\">" +
                "&copy; 2025 Lodgfy. Todos os direitos reservados." +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    // Pequena função para escapar caracteres que podem quebrar HTML simples
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    // ...existing code...
}
