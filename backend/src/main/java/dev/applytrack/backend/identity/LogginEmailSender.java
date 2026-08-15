package dev.applytrack.backend.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogginEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogginEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("Email to {} | subject: {} | body: {}", to, subject, body);
    }
}
