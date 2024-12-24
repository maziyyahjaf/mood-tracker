package com.example.maziyyah.mood_tracker.service;

import java.io.StringReader;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class ExternalLLMService {

    RestTemplate restTemplate = new RestTemplate();

    @Value("${llm.api.url}")
    private String llmApiUrl;

    @Value("${llm.api.key}")
    private String llmApiKey;

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    public String generateMessage(String context, String userName) {
        String prompt = "Based on the following mood summary, create an encouraging message for " + userName + " :\n"
                + context;
        System.out.println("Generated Prompt: " + prompt);
        String response = sendGeminiPrompt(prompt);
        System.out.println("Gemini Response: " + response);
        return response;
    }

    public String generateAdviceForTheDay(String summary) {
        String prompt = "Considering the following mood logs for the day, would you categorize my day as generally positive, neutral, or negative? Then, offer some tailored advice or encouragement: \n"
                + summary;
        String adviceResponse = sendGeminiAdvicePrompt(prompt);
        return adviceResponse;
    }

    public String sendGeminiPrompt(String prompt) {
        // Build the inner "text" object
        JsonObject text = Json.createObjectBuilder()
                .add("text", prompt)
                .build();

        // Build the "parts" array containing the "text" object
        JsonArray parts = Json.createArrayBuilder()
                .add(text)
                .build();

        // Build the "contents" array containing a single object with "role" and "parts"
        JsonObject content = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", parts)
                .build();

        JsonArray contents = Json.createArrayBuilder()
                .add(content)
                .build();

        // Build the root object with the model and "contents" array
        JsonObject root = Json.createObjectBuilder()
                .add("model", "models/gemini-pro")
                .add("contents", contents)
                .build();
        // send the request
        String parsedResponse = sendGeminiRequest(root);

        return parsedResponse;

    }

    public String sendGeminiAdvicePrompt(String prompt) {
        // Build the inner "text" object
        JsonObject text = Json.createObjectBuilder()
                .add("text", prompt)
                .build();

        // Build the "parts" array containing the "text" object
        JsonArray parts = Json.createArrayBuilder()
                .add(text)
                .build();

        // Build the "contents" array containing a single object with "role" and "parts"
        JsonObject content = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", parts)
                .build();

        JsonArray contents = Json.createArrayBuilder()
                .add(content)
                .build();

        JsonObject generationConfig = Json.createObjectBuilder()
                .add("maxOutputTokens", 150)
                .build();

        // Build the root object with the model and "contents" array, generationConfig
        // JSON
        JsonObject root = Json.createObjectBuilder()
                .add("model", "models/gemini-pro")
                .add("contents", contents)
                .add("generationConfig", generationConfig)
                .build();

        // send the request
        String parsedResponse = sendGeminiRequest(root);

        return parsedResponse;
    }

    public String sendGeminiRequest(JsonObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        RequestEntity<String> req = RequestEntity
                .post(URI.create(buildGenerateMessageUrl()))
                .headers(headers)
                .body(jsonObject.toString());

        try {
            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
            return parseGeneratedMessage(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to generate message using LLM: {}", e.getMessage());
            return null;
        }

    }

    public String parseGeneratedMessage(String responseBody) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
            // Parse the root JSON object
            JsonObject root = jsonReader.readObject();

            // Navigate through the JSON hierarchy
            JsonArray candidates = root.getJsonArray("candidates");
            JsonObject firstCandidate = candidates.getJsonObject(0); // Get the first candidate
            JsonObject content = firstCandidate.getJsonObject("content");
            JsonArray parts = content.getJsonArray("parts");
            JsonObject firstPart = parts.getJsonObject(0); // Get the first part
            String text = firstPart.getString("text"); // Extract the "text" field

            return text; // Return the extracted text
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse generated message", e);
        }
    }

    public String buildGenerateMessageUrl() {
        return UriComponentsBuilder.fromUriString(llmApiUrl)
                .queryParam("key", llmApiKey)
                .toUriString();

    }

}
