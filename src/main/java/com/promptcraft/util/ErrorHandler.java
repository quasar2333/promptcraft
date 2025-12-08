package com.promptcraft.util;

import com.promptcraft.PromptCraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

/**
 * Centralized error handling for PromptCraft (Forge 1.20.1)
 */
public class ErrorHandler {

    /**
     * Handles API-related errors
     */
    public static void handleApiError(String operation, Exception error, ServerPlayer player) {
        String message = "API Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable("promptcraft.api.error", error.getMessage())
                            .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Handles configuration errors
     */
    public static void handleConfigError(String operation, Exception error, ServerPlayer player) {
        String message = "Configuration Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable("promptcraft.config.error", error.getMessage())
                            .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Handles network errors
     */
    public static void handleNetworkError(String operation, Exception error, ServerPlayer player) {
        String message = "Network Error during " + operation + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (player != null) {
            player.sendSystemMessage(
                    Component.literal("Network error occurred. Please try again.")
                            .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Handles command execution errors
     */
    public static void handleCommandError(String command, Exception error, ServerPlayer player) {
        String message = "Command Execution Error for '" + command + "': " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable("promptcraft.command.error", error.getMessage())
                            .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Handles general errors with context
     */
    public static void handleError(String context, Exception error, ServerPlayer player) {
        String message = "Error in " + context + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (player != null) {
            player.sendSystemMessage(
                    Component.literal("An error occurred: " + error.getMessage())
                            .withStyle(ChatFormatting.RED));
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
        if (error instanceof java.net.SocketTimeoutException ||
                error instanceof java.net.ConnectException ||
                error instanceof java.io.IOException) {
            return true;
        }

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

        return "An unexpected error occurred: " + error.getMessage();
    }

    /**
     * Handles startup errors
     */
    public static void handleStartupError(String component, Exception error) {
        String message = "Failed to initialize " + component + ": " + error.getMessage();
        PromptCraft.LOGGER.error(message, error);

        if (isCriticalError(error)) {
            PromptCraft.LOGGER.error("Critical error detected. Mod functionality may be limited.");
        }
    }

    /**
     * Checks if an error is critical and might require mod shutdown
     */
    private static boolean isCriticalError(Exception error) {
        if (error.getMessage() != null &&
                error.getMessage().contains("configuration")) {
            return true;
        }

        if (error instanceof SecurityException) {
            return true;
        }

        return false;
    }
}
