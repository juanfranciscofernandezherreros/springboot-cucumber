package com.fernandez.backend.service;

public interface IEmailService {
    void sendEmail(String to, String subject, String text);
}

