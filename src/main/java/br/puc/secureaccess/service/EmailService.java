package br.puc.secureaccess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender sender;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> sender,
                        @Value("${app.mail.from:no-reply@secureaccess.local}") String from) {
        this.sender = sender.getIfAvailable();
        this.from = from;
    }

    public void sendPasswordReset(String to, String link) {
        if (sender == null) {
            log.warn("SMTP não configurado. Link de recuperação para {}: {}", to, link);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("SecureAccess - Recuperação de senha");
            msg.setText("Recebemos um pedido para redefinir sua senha.\n\n"
                    + "Acesse o link abaixo (válido por 30 minutos):\n" + link + "\n\n"
                    + "Se você não fez essa solicitação, ignore este email.");
            sender.send(msg);
        } catch (Exception e) {
            log.error("Falha ao enviar email de recuperação para {}: {}", to, e.getMessage());
        }
    }
}
