package com.promptcraft.client.api;

import com.promptcraft.api.SiliconFlowClient;
import com.promptcraft.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * Manages API calls for the client side
 */
@OnlyIn(Dist.CLIENT)
public class ApiManager {
    private static ApiManager instance;

    private SiliconFlowClient apiClient;

    private ApiManager() {
        this.apiClient = new SiliconFlowClient();
    }

    public static ApiManager getInstance() {
        if (instance == null) {
            instance = new ApiManager();
        }
        return instance;
    }

    /**
     * Generates a command using the AI API
     */
    public void generateCommand(String input, Consumer<CommandResult> callback) {
        // Check if API is configured
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        if (!config.isValid()) {
            Minecraft.getInstance().execute(() -> callback.accept(
                    new CommandResult(false, null, "API not configured. Please set your API key in settings.")));
            return;
        }

        // Make API call
        apiClient.generateCommand(input).thenAccept(response -> {
            Minecraft.getInstance().execute(() -> {
                if (response.isSuccess()) {
                    callback.accept(new CommandResult(true, response.getCommand(), null));
                } else {
                    callback.accept(new CommandResult(false, null, response.getError()));
                }
            });
        }).exceptionally(error -> {
            Minecraft.getInstance()
                    .execute(() -> callback.accept(new CommandResult(false, null, "API error: " + error.getMessage())));
            return null;
        });
    }

    /**
     * Shows a status message to the player
     */
    public void showStatusMessage(String message, boolean actionBar) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), actionBar);
        }
    }

    /**
     * Refreshes the API client with current config
     */
    public void refreshClient() {
        this.apiClient = new SiliconFlowClient();
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
