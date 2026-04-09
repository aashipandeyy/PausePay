package com.financeautopilot.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// takes a merchant name like "Swiggy", "Ola", "Apollo Pharmacy".
// checks Redis first - if "Swiggy → FOOD" is already stored, returns it instantly. No API call.
// if not in Redis - send merchant name to AI, get back a category, store in Redis, return the result.
// runs with @Async - never blocks the main thread.
// returns a CompletableFuture which is basically a "promise to give you the answer when it's ready."
@Service
@Slf4j
@RequiredArgsConstructor
public class CategorizationService {

    @Qualifier("openAiRestClient")
    private final RestClient openAiRestClient;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Async("categorizationExecutor")
    public CompletableFuture<String> categorize(String merchantName) {

        if(merchantName == null || merchantName.equals("UNKNOWN")) {
            return CompletableFuture.completedFuture("OTHER");
        }

        // check Redis cache first - key format: "merchant:swiggy" -> "food"
        String cacheKey = "merchant:" + merchantName.toLowerCase().trim();
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if(cached != null) {
            log.info("Cache hit for merchant: {}", merchantName);
            return CompletableFuture.completedFuture(cached);
        }

        // not in cache
        try {
            String prompt = """
                Classify this merchant into exactly one category.
                Categories: FOOD, TRANSPORT, SHOPPING, UTILITIES, HEALTH, ENTERTAINMENT, TRANSFER, OTHER
                Merchant: %s
                Reply with ONLY the category name, nothing else.
                """.formatted(merchantName);
            // model: "gpt-4o-mini", messages: [ {"role": "user"}, {"content": prompt} ]
            Map<String, Object> requestBody = Map.of(
                    "com/financeautopilot/financeautopilot/model", "gpt-4o-mini",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", prompt)
                    },
                    "max_tokens", 10
            );

            String responseJson = openAiRestClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            // res.choices[0].message.content
            String category = root.path("choices").get(0).path("message").path("content")
                    .asText().trim().toUpperCase();

            if(!isValidCategory(category)) category = "OTHER";
            redisTemplate.opsForValue().set(cacheKey, category);
            log.info("Categorized {} as {} and cached", merchantName, category);

            return CompletableFuture.completedFuture(category);
        }
        catch(Exception e) {
            log.info("Categorization failed for merchant: {}", merchantName, e);
            return CompletableFuture.completedFuture("OTHER");
        }
    }

    Boolean isValidCategory(String category) {
        return category.matches(
                "FOOD|TRANSPORT|SHOPPING|UTILITIES|HEALTH|ENTERTAINMENT|TRANSFER|OTHER"
        );
    }
}
