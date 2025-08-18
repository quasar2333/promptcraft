package com.promptcraft.util;

import com.promptcraft.PromptCraft;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Centralized error handling for PromptCraft
 */
public class ErrorHandler {
    
    /**
     * Handles API-related errors
     */
    public static void handleApiError(String operation, Exception error, ServerPlayerEntity player) {
        String message = "API Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        if (player != null) {
            player.sendMessage(
                Text.translatable("promptcraft.api.error", error.getMessage()).formatted(Formatting.RED), 
                false
            );
        }
    }
    
    /**
     * Handles configuration errors
     */
    public static void handleConfigError(String operation, Exception error, ServerPlayerEntity player) {
        String message = "Configuration Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        if (player != null) {
            player.sendMessage(
                Text.translatable("promptcraft.config.error", error.getMessage()).formatted(Formatting.RED), 
                false
            );
        }
    }
    
    /**
     * Handles network errors
     */
    public static void handleNetworkError(String operation, Exception error, ServerPlayerEntity player) {
        String message = "Network Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        if (player != null) {
            player.sendMessage(
                Text.literal("Network error occurred. Please try again.").formatted(Formatting.RED), 
                false
            );
        }
    }
    
    /**
     * Handles command execution errors
     */
    public static void handleCommandError(String command, Exception error, ServerPlayerEntity player) {
        String message = "Command Execution Error for '" + command + "': " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        if (player != null) {
            player.sendMessage(
                Text.translatable("promptcraft.command.error", error.getMessage()).formatted(Formatting.RED), 
                false
            );
        }
    }
    
    /**
     * Handles general errors with context
     */
    public static void handleError(String context, Exception error, ServerPlayerEntity player) {
        String message = "Error in " + context + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        if (player != null) {
            player.sendMessage(
                Text.literal("An error occurred: " + error.getMessage()).formatted(Formatting.RED), 
                false
            );
        }
    }
    
    /**
     * Logs a warning message
     */
    public static void logWarning(String message) {
        PromptCraft.LOGGER.warn(message);
    }
    
    /**
     * Logs a warning message with context
     */
    public static void logWarning(String context, String message) {
        PromptCraft.LOGGER.warn("[{}] {}", context, message);
    }
    
    /**
     * Checks if an error is recoverable
     */
    public static boolean isRecoverableError(Exception error) {
        // Network timeouts and connection errors are usually recoverable
        if (error instanceof java.net.SocketTimeoutException ||
            error instanceof java.net.ConnectException ||
            error instanceof java.io.IOException) {
            return true;
        }
        
        // Configuration errors might be recoverable if user fixes config
        if (error.getMessage() != null && 
            (error.getMessage().contains("configuration") || 
             error.getMessage().contains("config"))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets a user-friendly error message
     */
    public static String getUserFriendlyMessage(Exception error) {
        if (error instanceof java.net.SocketTimeoutException) {
            return "Request timed out. Please check your internet connection and try again.";
        }
        
        if (error instanceof java.net.ConnectException) {
            return "Could not connect to the API server. Please check your network connection.";
        }
        
        if (error instanceof java.io.IOException) {
            return "Network error occurred. Please try again later.";
        }
        
        if (error.getMessage() != null && error.getMessage().contains("API key")) {
            return "Invalid API key. Please check your configuration.";
        }
        
        if (error.getMessage() != null && error.getMessage().contains("model")) {
            return "Invalid model specified. Please check your configuration.";
        }
        
        // Default message
        return "An unexpected error occurred: " + error.getMessage();
    }
    
    /**
     * Handles startup errors
     */
    public static void handleStartupError(String component, Exception error) {
        String message = "Failed to initialize " + component + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);
        
        // For critical startup errors, we might want to disable the mod
        if (isCriticalError(error)) {
            PromptCraft.LOGGER.error("Critical error detected. Mod functionality may be limited.");
        }
    }
    
    /**
     * Checks if an error is critical and might require mod shutdown
     */
    private static boolean isCriticalError(Exception error) {
        // Configuration loading errors are critical
        if (error.getMessage() != null && 
            error.getMessage().contains("configuration")) {
            return true;
        }
        
        // Security-related errors are critical
        if (error instanceof SecurityException) {
            return true;
        }
        
        return false;
    }
}
