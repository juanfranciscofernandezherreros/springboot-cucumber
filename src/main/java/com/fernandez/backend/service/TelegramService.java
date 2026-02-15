package com.fernandez.backend.service;

import com.fernandez.backend.utils.constants.ServiceStrings;
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
                ServiceStrings.Telegram.URL_FORMAT,
                botToken
        );

        Map<String, String> body = new HashMap<>();
        body.put(ServiceStrings.Telegram.FIELD_CHAT_ID, chatId);
        body.put(ServiceStrings.Telegram.FIELD_TEXT, message);
        body.put(ServiceStrings.Telegram.FIELD_PARSE_MODE, ServiceStrings.Telegram.PARSE_MODE_HTML);
        restTemplate.postForEntity(url, body, String.class);
    }
}
