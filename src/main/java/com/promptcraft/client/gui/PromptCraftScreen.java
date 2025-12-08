package com.promptcraft.client.gui;

import com.promptcraft.PromptCraft;
import com.promptcraft.client.api.ApiManager;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Main GUI screen for PromptCraft AI command generation (NeoForge)
 */
@OnlyIn(Dist.CLIENT)
public class PromptCraftScreen extends Screen {
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 260;

    private EditBox inputField;
    private Button generateButton;
    private Button copyButton;
    private Button executeButton;
    private Button configButton;
    private Button commandBlockToggle;

    private String generatedCommand = "";
    private boolean useCommandBlocks = false;
    private boolean isGenerating = false;

    public PromptCraftScreen() {
        super(Component.translatable("gui.promptcraft.title"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        // Input field for user prompt
        this.inputField = new EditBox(
                this.font,
                centerX - 150,
                startY + 32,
                300,
                18,
                Component.literal(""));
        this.inputField.setMaxLength(500);
        this.inputField.setHint(Component.translatable("gui.promptcraft.input.placeholder"));
        this.addRenderableWidget(this.inputField);
        this.setInitialFocus(this.inputField);

        // Generate button
        this.generateButton = Button.builder(
                Component.translatable("gui.promptcraft.generate"),
                button -> generateCommand())
                .bounds(centerX - 140, startY + 56, 80, 18)
                .build();
        this.addRenderableWidget(this.generateButton);

        // Command block toggle
        this.commandBlockToggle = Button.builder(
                Component.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF"),
                button -> toggleCommandBlock())
                .bounds(centerX - 50, startY + 56, 80, 18)
                .build();
        this.addRenderableWidget(this.commandBlockToggle);

        // Config button
        this.configButton = Button.builder(
                Component.translatable("gui.promptcraft.config"),
                button -> openConfig())
                .bounds(centerX + 40, startY + 56, 50, 18)
                .build();
        this.addRenderableWidget(this.configButton);

        // Blacklist config button
        Button blacklistButton = Button.builder(
                Component.translatable("gui.promptcraft.blacklist"),
                button -> openBlacklistConfig())
                .bounds(centerX + 100, startY + 56, 60, 18)
                .build();
        this.addRenderableWidget(blacklistButton);

        // Copy button
        this.copyButton = Button.builder(
                Component.translatable("gui.promptcraft.copy"),
                button -> copyCommand())
                .bounds(centerX - 70, startY + 160, 65, 18)
                .build();
        this.copyButton.active = false;
        this.addRenderableWidget(this.copyButton);

        // Execute button
        this.executeButton = Button.builder(
                Component.translatable("gui.promptcraft.execute"),
                button -> executeCommand())
                .bounds(centerX + 10, startY + 160, 65, 18)
                .build();
        this.executeButton.active = false;
        this.addRenderableWidget(this.executeButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        // Render widgets first
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Title
        Component title = Component.translatable("gui.promptcraft.title");
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, centerX - titleWidth / 2, startY - 10, 0xFFFFFFFF, true);

        // Draw generated command or status
        if (isGenerating) {
            Component generating = Component.translatable("gui.promptcraft.generating")
                    .withStyle(ChatFormatting.YELLOW);
            int textWidth = this.font.width(generating);
            guiGraphics.drawString(this.font, generating, centerX - textWidth / 2, startY + 84, 0xFFFFFFFF, true);
        } else if (!generatedCommand.isEmpty()) {
            // Draw command with word wrapping
            drawWrappedText(guiGraphics, generatedCommand, centerX - 140, startY + 84, 280, 0xFFFFFFFF);
        } else {
            Component placeholder = Component.translatable("gui.promptcraft.result.placeholder")
                    .withStyle(ChatFormatting.GRAY);
            int textWidth = this.font.width(placeholder);
            guiGraphics.drawString(this.font, placeholder, centerX - textWidth / 2, startY + 84, 0xFFFFFFFF, true);
        }
    }

    private void drawWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (this.font.width(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                if (currentLine.length() > 0) {
                    Component lineText = Component.literal(currentLine.toString());
                    int lineWidth = this.font.width(lineText);
                    guiGraphics.drawString(this.font, lineText, x + (maxWidth - lineWidth) / 2, currentY, color, true);
                    currentY += 10;
                }
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            Component lineText = Component.literal(currentLine.toString());
            int lineWidth = this.font.width(lineText);
            guiGraphics.drawString(this.font, lineText, x + (maxWidth - lineWidth) / 2, currentY, color | 0xFF000000,
                    true);
        }
    }

    private void generateCommand() {
        String input = this.inputField.getValue().trim();
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
                Component.translatable("gui.promptcraft.command_block", useCommandBlocks ? "ON" : "OFF"));
    }

    private void copyCommand() {
        if (!generatedCommand.isEmpty() && this.minecraft != null) {
            this.minecraft.keyboardHandler.setClipboard(generatedCommand);
            // Show copy confirmation message
            ApiManager.getInstance().showStatusMessage("Command copied to clipboard!", false);
        }
    }

    private void executeCommand() {
        if (!generatedCommand.isEmpty() && !generatedCommand.startsWith("Error:")) {
            // Check if confirmation is required
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
            if (config.requireConfirmation && this.minecraft != null) {
                // Show confirmation dialog
                this.minecraft.setScreen(new ConfirmationScreen(this, generatedCommand,
                        this::doExecuteCommand, null));
            } else {
                doExecuteCommand();
            }
        }
    }

    private void doExecuteCommand() {
        // Send command execution packet to server
        NetworkHandler.sendExecuteCommand(generatedCommand, useCommandBlocks);

        // Show feedback to user
        ApiManager.getInstance().showStatusMessage("Command sent for execution: " + generatedCommand, false);

        this.onClose();
    }

    private void openConfig() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ConfigScreen(this));
        }
    }

    private void openBlacklistConfig() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new BlacklistScreen(this));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }
}
