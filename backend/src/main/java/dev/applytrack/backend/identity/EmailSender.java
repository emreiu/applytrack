package dev.applytrack.backend.identity;

public interface EmailSender {

    void send(String to, String subject, String body);
}
