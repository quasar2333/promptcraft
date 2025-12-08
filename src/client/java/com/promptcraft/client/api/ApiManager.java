package com.promptcraft.client.api;

import com.promptcraft.PromptCraft;
import com.promptcraft.api.SiliconFlowClient;
import com.promptcraft.client.util.PerformanceMonitor;
import com.promptcraft.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Manages API calls and provides client-side integration
 */
public class ApiManager {
    private static volatile ApiManager instance;
    private static final Object LOCK = new Object();
    private SiliconFlowClient apiClient;

    private ApiManager() {
        this.apiClient = new SiliconFlowClient();
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ApiManager();
                }
            }
        }
        return instance;
    }

    /**
     * Refreshes the API client with current configuration
     */
    public void refreshClient() {
        this.apiClient = new SiliconFlowClient();
    }

    /**
     * Generates a command asynchronously and calls the callback with the result
     */
    public void generateCommand(String userInput, Consumer<CommandResult> callback) {
        // Validate configuration
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        if (!config.isValid()) {
            callback.accept(new CommandResult(false, null, "Invalid configuration. Please check your API settings."));
            return;
        }

        if (userInput == null || userInput.trim().isEmpty()) {
            callback.accept(new CommandResult(false, null, "Input cannot be empty."));
            return;
        }

        // Log the request if logging is enabled
        if (config.enableLogging) {
            PromptCraft.LOGGER.info("Generating command for input: {}", userInput);
        }

        // Start performance monitoring
        PerformanceMonitor.startTiming("api_call");
        PerformanceMonitor.logMemoryUsage("before_api_call");

        // Make API call
        CompletableFuture<SiliconFlowClient.ApiResponse> future = apiClient.generateCommand(userInput);

        future.thenAccept(response -> {
            // End performance monitoring
            PerformanceMonitor.endTiming("api_call");
            PerformanceMonitor.logMemoryUsage("after_api_call");

            // Execute callback on main thread
            MinecraftClient.getInstance().execute(() -> {
                if (response.isSuccess()) {
                    String command = response.getCommand();

                    // Validate command
                    if (isCommandValid(command)) {
                        if (config.enableLogging) {
                            PromptCraft.LOGGER.info("Generated command: {}", command);
                        }
                        callback.accept(new CommandResult(true, command, null));
                    } else {
                        String error = "Generated command is invalid or potentially dangerous: " + command;
                        PromptCraft.LOGGER.warn(error);
                        callback.accept(new CommandResult(false, null, error));
                    }
                } else {
                    String error = response.getError();
                    PromptCraft.LOGGER.error("API call failed: {}", error);
                    callback.accept(new CommandResult(false, null, error));
                }

                // Suggest garbage collection if memory usage is high
                PerformanceMonitor.suggestGarbageCollection();
            });
        }).exceptionally(throwable -> {
            // End performance monitoring on error
            PerformanceMonitor.endTiming("api_call");

            // Handle exceptions
            MinecraftClient.getInstance().execute(() -> {
                String error = "Unexpected error: " + throwable.getMessage();
                PromptCraft.LOGGER.error("API call exception", throwable);
                callback.accept(new CommandResult(false, null, error));
            });
            return null;
        });
    }

    /**
     * Validates if a command is safe to execute
     */
    private boolean isCommandValid(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        command = command.trim();

        // Must start with /
        if (!command.startsWith("/")) {
            return false;
        }

        // Check against blacklist
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        String lowerCommand = command.toLowerCase();

        for (String keyword : config.blacklistedKeywords) {
            if (lowerCommand.contains(keyword.toLowerCase())) {
                return false;
            }
        }

        // Additional safety checks
        if (command.length() > config.maxCommandLength) {
            return false;
        }

        // Check for potentially dangerous patterns
        String[] dangerousPatterns = {
                "rm -rf",
                "format",
                "del /",
                "shutdown",
                "halt",
                "reboot",
                "kill -9",
                "sudo rm",
                "dd if=",
                ":(){ :|:& };:"
        };

        for (String pattern : dangerousPatterns) {
            if (lowerCommand.contains(pattern.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Shows a status message to the player
     */
    public void showStatusMessage(String message, boolean isError) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text text;
            if (isError) {
                text = Text.literal(message).formatted(Formatting.RED);
            } else {
                text = Text.literal(message).formatted(Formatting.GREEN);
            }
            client.player.sendMessage(text, false);
        }
    }

    /**
     * Result of a command generation request
     */
    public static class CommandResult {
        private final boolean success;
        private final String command;
        private final String error;

        public CommandResult(boolean success, String command, String error) {
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
