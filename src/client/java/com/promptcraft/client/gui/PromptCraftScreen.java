package com.promptcraft.client.gui;

import com.promptcraft.PromptCraft;
import com.promptcraft.client.api.ApiManager;
import com.promptcraft.config.ConfigManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Main GUI screen for PromptCraft AI command generation
 */
public class PromptCraftScreen extends Screen {
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 260;

    private TextFieldWidget inputField;
    private ButtonWidget generateButton;
    private ButtonWidget copyButton;
    private ButtonWidget executeButton;
    private ButtonWidget configButton;
    private ButtonWidget commandBlockToggle;

    private String generatedCommand = "";
    private boolean useCommandBlocks = false;
    private boolean isGenerating = false;

    public PromptCraftScreen() {
        super(Text.translatable("gui.promptcraft.title"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        // Input field for user prompt
        this.inputField = new TextFieldWidget(
                this.textRenderer,
                centerX - 150,
                startY + 32,
                300,
                18,
                Text.literal(""));
        this.inputField.setMaxLength(500);
        this.inputField.setEditable(true);
        this.inputField.setVisible(true);
        // Make input field visually blend with transparent style
        try {
            this.inputField.setDrawsBackground(false);
        } catch (Throwable ignored) {
        }
        // Use suggestion (native placeholder) so it renders correctly inside the widget
        // and disappears on typing
        this.inputField.setSuggestion(Text.translatable("gui.promptcraft.input.placeholder").getString());
        this.inputField.setChangedListener(text -> {
            if (text == null || text.isEmpty()) {
                this.inputField.setSuggestion(Text.translatable("gui.promptcraft.input.placeholder").getString());
            } else {
                this.inputField.setSuggestion(null);
            }
        });
        // Register as a drawable child once to ensure proper z-order and blur handling
        this.addDrawableChild(this.inputField);
        this.setInitialFocus(this.inputField);

        // Generate button
        this.generateButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.generate"),
                button -> generateCommand()).dimensions(centerX - 140, startY + 56, 80, 18).build();
        this.addDrawableChild(this.generateButton);

        // Command block toggle
        this.commandBlockToggle = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF"),
                button -> toggleCommandBlock()).dimensions(centerX - 50, startY + 56, 80, 18).build();
        this.addDrawableChild(this.commandBlockToggle);

        // Config button
        this.configButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.config"),
                button -> openConfig()).dimensions(centerX + 40, startY + 56, 50, 18).build();
        this.addDrawableChild(this.configButton);

        // Blacklist config button
        ButtonWidget blacklistButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.blacklist"),
                button -> openBlacklistConfig()).dimensions(centerX + 100, startY + 56, 60, 18).build();
        this.addDrawableChild(blacklistButton);

        // Copy button
        this.copyButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.copy"),
                button -> copyCommand()).dimensions(centerX - 70, startY + 160, 65, 18).build();
        this.copyButton.active = false;
        this.addDrawableChild(this.copyButton);

        // Execute button
        this.executeButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.execute"),
                button -> executeCommand()).dimensions(centerX + 10, startY + 160, 65, 18).build();
        this.executeButton.active = false;
        this.addDrawableChild(this.executeButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Transparent background (no overlay)

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        // Render widgets first - all controls on same layer
        super.render(context, mouseX, mouseY, delta);

        // Draw all text content AFTER widgets to ensure same layer
        // Title
        Text title = Text.translatable("gui.promptcraft.title");
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, centerX - titleWidth / 2, startY - 10, 0xFFFFFFFF, true);

        // Draw generated command or status (same layer as widgets)
        if (isGenerating) {
            Text generating = Text.translatable("gui.promptcraft.generating").formatted(Formatting.YELLOW);
            // Use direct text rendering to avoid API compatibility issues
            int textWidth = this.textRenderer.getWidth(generating);
            context.drawText(this.textRenderer, generating, centerX - textWidth / 2, startY + 84, 0xFFFFFFFF, true);
        } else if (!generatedCommand.isEmpty()) {
            // Draw command with word wrapping
            drawWrappedText(context, generatedCommand, centerX - 140, startY + 84, 280, 0xFFFFFFFF);
        } else {
            Text placeholder = Text.translatable("gui.promptcraft.result.placeholder").formatted(Formatting.GRAY);
            // Use direct text rendering to avoid API compatibility issues
            int textWidth = this.textRenderer.getWidth(placeholder);
            context.drawText(this.textRenderer, placeholder, centerX - textWidth / 2, startY + 84, 0xFFFFFFFF, true);
        }
    }

    private void drawWrappedText(DrawContext context, String text, int x, int y, int maxWidth, int color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (this.textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                if (currentLine.length() > 0) {
                    // Use direct text rendering with manual centering
                    Text lineText = Text.literal(currentLine.toString());
                    int lineWidth = this.textRenderer.getWidth(lineText);
                    context.drawText(this.textRenderer, lineText, x + (maxWidth - lineWidth) / 2, currentY, color,
                            true);
                    currentY += 10;
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            // Use direct text rendering with manual centering
            Text lineText = Text.literal(currentLine.toString());
            int lineWidth = this.textRenderer.getWidth(lineText);
            context.drawText(this.textRenderer, lineText, x + (maxWidth - lineWidth) / 2, currentY,
                    (color | 0xFF000000), true);
        }
    }

    private void generateCommand() {
        String input = this.inputField.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        this.isGenerating = true;
        this.generateButton.active = false;
        this.copyButton.active = false;
        this.executeButton.active = false;
        this.generatedCommand = "";

        // Make actual API call
        ApiManager.getInstance().generateCommand(input, result -> {
            this.isGenerating = false;
            this.generateButton.active = true;

            if (result.isSuccess()) {
                this.generatedCommand = result.getCommand();
                this.copyButton.active = true;
                this.executeButton.active = true;
            } else {
                this.generatedCommand = "Error: " + result.getError();
                this.copyButton.active = false;
                this.executeButton.active = false;
            }
        });
    }

    private void toggleCommandBlock() {
        this.useCommandBlocks = !this.useCommandBlocks;
        this.commandBlockToggle.setMessage(
                Text.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF"));
    }

    private void copyCommand() {
        if (!generatedCommand.isEmpty() && this.client != null) {
            this.client.keyboard.setClipboard(generatedCommand);
            // Show copy confirmation message
            ApiManager.getInstance().showStatusMessage(
                    "Command copied to clipboard!", false);
        }
    }

    private void executeCommand() {
        if (!generatedCommand.isEmpty() && !generatedCommand.startsWith("Error:")) {
            // Check if confirmation is required
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
            if (config.requireConfirmation && this.client != null) {
                // Show confirmation dialog
                this.client.setScreen(new ConfirmationScreen(this, generatedCommand,
                        this::doExecuteCommand, null));
            } else {
                doExecuteCommand();
            }
        }
    }

    private void doExecuteCommand() {
        // Send command execution packet to server
        com.promptcraft.client.network.ClientNetworkHandler.sendExecuteCommand(generatedCommand, useCommandBlocks);

        // Show feedback to user
        ApiManager.getInstance().showStatusMessage("Command sent for execution: " + generatedCommand, false);

        this.close();
    }

    private void openConfig() {
        if (this.client != null) {
            this.client.setScreen(new ConfigScreen(this));
        }
    }

    private void openBlacklistConfig() {
        if (this.client != null) {
            this.client.setScreen(new BlacklistScreen(this));
        }
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}
