package com.fernandez.backend.application.service;

import com.fernandez.backend.application.port.in.ITelegramService;
import com.fernandez.backend.shared.constants.ServiceStrings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// Sin @Service: este bean se crea explícitamente en ServiceBeansConfig
@RequiredArgsConstructor
public class TelegramService implements ITelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        String url = String.format(ServiceStrings.Telegram.URL_FORMAT, botToken);

        Map<String, String> body = new HashMap<>();
        body.put(ServiceStrings.Telegram.FIELD_CHAT_ID, chatId);
        body.put(ServiceStrings.Telegram.FIELD_TEXT, message);
        body.put(ServiceStrings.Telegram.FIELD_PARSE_MODE, ServiceStrings.Telegram.PARSE_MODE_HTML);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, entity, String.class);
    }
}
