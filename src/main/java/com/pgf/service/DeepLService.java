package com.pgf.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class DeepLService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public DeepLService(RestTemplate restTemplate,
                        @Value("${deepl.api.key}") String apiKey,
                        @Value("${deepl.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String translate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "DeepL-Auth-Key " + apiKey);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("text", text);
            body.add("source_lang", "FR");
            body.add("target_lang", "EN");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            DeepLResponse response = restTemplate.postForObject(apiUrl, request, DeepLResponse.class);

            if (response == null || response.getTranslations().isEmpty()) {
                return null;
            }
            return response.getTranslations().get(0).getText();
        } catch (Exception e) {
            log.error("DeepL translation failed for text: {}", text, e);
            return null;
        }
    }

    @Getter
    private static class DeepLResponse {
        private List<Translation> translations;

        @Getter
        private static class Translation {
            private String text;
        }
    }
}