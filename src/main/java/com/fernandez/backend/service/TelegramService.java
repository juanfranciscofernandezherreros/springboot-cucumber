package com.fernandez.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendMessage(String message) {
        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        Map<String, String> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", message);
        body.put("parse_mode", "HTML");
        restTemplate.postForEntity(url, body, String.class);
    }
}

