package com.financeautopilot.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategorizationService {

    @Qualifier("openAiRestClient")
    private final RestClient openAiRestClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String categorizeSync(String merchantName) {
        if (merchantName == null || merchantName.equalsIgnoreCase("UNKNOWN")) {
            return "OTHER";
        }

        String cacheKey = "merchant:" + merchantName.toLowerCase().trim();
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache hit for merchant: {}", merchantName);
            return cached;
        }

        try {
            String prompt = """
                    Classify this merchant into exactly one category.
                    Categories: FOOD, TRANSPORT, SHOPPING, UTILITIES, HEALTH, ENTERTAINMENT, TRANSFER, OTHER
                    Merchant: %s
                    Reply with ONLY the category name, nothing else.
                    """.formatted(merchantName);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", new Object[]{Map.of("role", "user", "content", prompt)},
                    "max_tokens", 10
            );

            String responseJson = openAiRestClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            String category = root.path("choices").get(0).path("message").path("content")
                    .asText().trim().toUpperCase();

            if (!isValidCategory(category)) {
                category = "OTHER";
            }

            redisTemplate.opsForValue().set(cacheKey, category);
            return category;
        } catch (Exception e) {
            log.warn("Categorization failed for merchant: {}", merchantName, e);
            return "OTHER";
        }
    }

    Boolean isValidCategory(String category) {
        return category.matches(
                "FOOD|TRANSPORT|SHOPPING|UTILITIES|HEALTH|ENTERTAINMENT|TRANSFER|OTHER");
    }
}
