package com.fernandez.backend.application.port.in;

public interface IEmailService {
    void sendEmail(String to, String subject, String text);
}

