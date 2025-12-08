package com.promptcraft.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Client for interacting with SiliconFlow API
 * Supports both OpenAI and Google API formats
 */
public class SiliconFlowClient {
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final ConfigManager.PromptCraftConfig config;

    public SiliconFlowClient() {
        this.config = ConfigManager.getConfig();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
                .build();
    }

    /**
     * Generates a Minecraft command based on the user's natural language input
     */
    public CompletableFuture<ApiResponse> generateCommand(String userInput) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generateCommandSync(userInput);
            } catch (Exception e) {
                PromptCraft.LOGGER.error("Error generating command", e);
                return new ApiResponse(false, null, "API call failed: " + e.getMessage());
            }
        });
    }

    private ApiResponse generateCommandSync(String userInput) throws IOException {
        String prompt = buildPrompt(userInput);

        // Try with retries
        IOException lastException = null;
        for (int attempt = 0; attempt < config.maxRetries; attempt++) {
            try {
                return makeApiCall(prompt);
            } catch (IOException e) {
                lastException = e;
                PromptCraft.LOGGER.warn("API call attempt {} failed: {}", attempt + 1, e.getMessage());

                if (attempt < config.maxRetries - 1) {
                    try {
                        Thread.sleep(1000 * (attempt + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw lastException != null ? lastException : new IOException("All retry attempts failed");
    }

    private String buildPrompt(String userInput) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Minecraft command generator. ");
        prompt.append("Generate a single, valid Minecraft command based on the user's request. ");
        prompt.append("Only respond with the command itself, no explanations or additional text. ");
        prompt.append("The command should start with '/' and be executable in Minecraft. ");
        prompt.append(
                "If the request is unclear or impossible, respond with '/say Unable to generate command for this request'.\n\n");
        prompt.append("User request: ").append(userInput);

        return prompt.toString();
    }

    private ApiResponse makeApiCall(String prompt) throws IOException {
        // Build request body for OpenAI-compatible format
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", config.modelId);
        requestBody.addProperty("max_tokens", 150);
        requestBody.addProperty("temperature", 0.3);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        requestBody.add("messages", messages);

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl + "/chat/completions"))
                .header("Authorization", "Bearer " + config.apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(config.timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody)))
                .build();

        // Execute request
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("API call failed with status " + response.statusCode() + ": " + response.body());
            }

            return parseResponse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("API call was interrupted", e);
        }
    }

    private ApiResponse parseResponse(String responseBody) {
        try {
            JsonObject jsonResponse = GSON.fromJson(responseBody, JsonObject.class);

            // Parse OpenAI-style response
            if (jsonResponse.has("choices")) {
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        JsonObject message = firstChoice.getAsJsonObject("message");
                        String content = message.get("content").getAsString().trim();

                        // Clean up the response
                        String command = cleanCommand(content);
                        return new ApiResponse(true, command, null);
                    }
                }
            }

            // Parse Google-style response (if needed)
            if (jsonResponse.has("candidates")) {
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    if (firstCandidate.has("content")) {
                        JsonObject content = firstCandidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                String text = parts.get(0).getAsJsonObject().get("text").getAsString().trim();
                                String command = cleanCommand(text);
                                return new ApiResponse(true, command, null);
                            }
                        }
                    }
                }
            }

            return new ApiResponse(false, null, "Invalid response format");

        } catch (Exception e) {
            PromptCraft.LOGGER.error("Error parsing API response", e);
            return new ApiResponse(false, null, "Failed to parse response: " + e.getMessage());
        }
    }

    private String cleanCommand(String rawCommand) {
        // Remove any markdown formatting
        rawCommand = rawCommand.replaceAll("```[a-zA-Z]*\\n?", "").replaceAll("```", "");

        // Remove extra whitespace
        rawCommand = rawCommand.trim();

        // Ensure command starts with /
        if (!rawCommand.startsWith("/")) {
            // Try to find a command in the text
            String[] lines = rawCommand.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("/")) {
                    rawCommand = line;
                    break;
                }
            }

            // If still no command found, add / prefix
            if (!rawCommand.startsWith("/")) {
                rawCommand = "/" + rawCommand;
            }
        }

        // Take only the first line if multiple lines
        if (rawCommand.contains("\n")) {
            rawCommand = rawCommand.split("\n")[0].trim();
        }

        // Validate command length
        if (rawCommand.length() > config.maxCommandLength) {
            rawCommand = rawCommand.substring(0, config.maxCommandLength);
        }

        return rawCommand;
    }

    /**
     * Response from the API
     */
    public static class ApiResponse {
        private final boolean success;
        private final String command;
        private final String error;

        public ApiResponse(boolean success, String command, String error) {
            this.success = success;
            this.command = command;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getCommand() {
            return command;
        }

        public String getError() {
            return error;
        }
    }
}
