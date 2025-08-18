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
    private static final int WINDOW_WIDTH = 480;
    private static final int WINDOW_HEIGHT = 320;
    
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
            centerX - 220,
            startY + 42,
            440,
            20,
            Text.literal("")
        );
        this.inputField.setMaxLength(500);
        this.inputField.setEditable(true);
        this.inputField.setVisible(true);
        // Make input field visually blend with transparent style
        try { this.inputField.setDrawsBackground(false); } catch (Throwable ignored) {}
        // Use suggestion (native placeholder) so it renders correctly inside the widget
        this.inputField.setSuggestion(Text.translatable("gui.promptcraft.input.placeholder").getString());
        // Register as a drawable child once to ensure proper z-order and blur handling
        this.addDrawableChild(this.inputField);
        this.setInitialFocus(this.inputField);
        
        // Generate button
        this.generateButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.generate"),
            button -> generateCommand()
        ).dimensions(centerX - 180, startY + 70, 100, 20).build();
        this.addDrawableChild(this.generateButton);
        
        // Command block toggle
        this.commandBlockToggle = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF"),
            button -> toggleCommandBlock()
        ).dimensions(centerX - 70, startY + 70, 100, 20).build();
        this.addDrawableChild(this.commandBlockToggle);
        
        // Config button
        this.configButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.config"),
            button -> openConfig()
        ).dimensions(centerX + 40, startY + 70, 60, 20).build();
        this.addDrawableChild(this.configButton);
        
        // Blacklist config button
        ButtonWidget blacklistButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.blacklist"),
            button -> openBlacklistConfig()
        ).dimensions(centerX + 110, startY + 70, 70, 20).build();
        this.addDrawableChild(blacklistButton);
        
        // Copy button
        this.copyButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.copy"),
            button -> copyCommand()
        ).dimensions(centerX - 100, startY + 200, 80, 20).build();
        this.copyButton.active = false;
        this.addDrawableChild(this.copyButton);
        
        // Execute button
        this.executeButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.execute"),
            button -> executeCommand()
        ).dimensions(centerX + 20, startY + 200, 80, 20).build();
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
        context.drawCenteredTextWithShadow(this.textRenderer, title, centerX, startY - 10, 0xFFFFFF);

        // Draw generated command or status (same layer as widgets)
        if (isGenerating) {
            Text generating = Text.translatable("gui.promptcraft.generating").formatted(Formatting.YELLOW);
            context.drawTextWithShadow(this.textRenderer, generating, centerX - 170, startY + 110, 0xFFFFFF);
        } else if (!generatedCommand.isEmpty()) {
            // Draw command with word wrapping
            drawWrappedText(context, generatedCommand, centerX - 170, startY + 110, 340, 0xFFFFFF);
        } else {
            Text placeholder = Text.translatable("gui.promptcraft.result.placeholder").formatted(Formatting.GRAY);
            context.drawTextWithShadow(this.textRenderer, placeholder, centerX - 170, startY + 110, 0xFFFFFF);
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
                    context.drawTextWithShadow(this.textRenderer, currentLine.toString(), x, currentY, color);
                    currentY += 10;
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            context.drawTextWithShadow(this.textRenderer, currentLine.toString(), x, currentY, color);
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
            Text.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF")
        );
    }
    
    private void copyCommand() {
        if (!generatedCommand.isEmpty()) {
            this.client.keyboard.setClipboard(generatedCommand);
            // TODO: Show copy confirmation
        }
    }
    
    private void executeCommand() {
        if (!generatedCommand.isEmpty() && !generatedCommand.startsWith("Error:")) {
            // Check if confirmation is required
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
            if (config.requireConfirmation) {
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
        ClientPlayNetworking.send(new com.promptcraft.network.NetworkHandler.ExecuteCommandPayload(generatedCommand, useCommandBlocks));

        // Show feedback to user
        ApiManager.getInstance().showStatusMessage("Command sent for execution: " + generatedCommand, false);

        this.close();
    }
    
    private void openConfig() {
        this.client.setScreen(new ConfigScreen(this));
    }

    private void openBlacklistConfig() {
        this.client.setScreen(new BlacklistScreen(this));
    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}
