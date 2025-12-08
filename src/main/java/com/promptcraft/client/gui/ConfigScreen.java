package com.promptcraft.client.gui;

import com.promptcraft.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Configuration screen for PromptCraft settings
 */
@OnlyIn(Dist.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;

    private EditBox apiKeyField;
    private EditBox modelIdField;
    private EditBox baseUrlField;
    private Button confirmationToggle;

    private ConfigManager.PromptCraftConfig config;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("gui.promptcraft.config.title"));
        this.parent = parent;
        this.config = ConfigManager.getConfig();
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 50;
        int fieldWidth = 200;

        // API Key field
        this.apiKeyField = new EditBox(this.font, centerX - fieldWidth / 2, startY, fieldWidth, 18,
                Component.translatable("gui.promptcraft.config.api_key"));
        this.apiKeyField.setMaxLength(200);
        this.apiKeyField.setValue(config.apiKey);
        this.addRenderableWidget(this.apiKeyField);

        // Model ID field
        this.modelIdField = new EditBox(this.font, centerX - fieldWidth / 2, startY + 35, fieldWidth, 18,
                Component.translatable("gui.promptcraft.config.model"));
        this.modelIdField.setMaxLength(100);
        this.modelIdField.setValue(config.modelId);
        this.addRenderableWidget(this.modelIdField);

        // Base URL field
        this.baseUrlField = new EditBox(this.font, centerX - fieldWidth / 2, startY + 70, fieldWidth, 18,
                Component.translatable("gui.promptcraft.config.base_url"));
        this.baseUrlField.setMaxLength(200);
        this.baseUrlField.setValue(config.baseUrl);
        this.addRenderableWidget(this.baseUrlField);

        // Confirmation toggle
        this.confirmationToggle = Button.builder(
                Component.translatable("gui.promptcraft.config.confirmation",
                        config.requireConfirmation ? "ON" : "OFF"),
                button -> toggleConfirmation())
                .bounds(centerX - 75, startY + 105, 150, 20)
                .build();
        this.addRenderableWidget(this.confirmationToggle);

        // Save button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.config.save"),
                button -> saveAndClose())
                .bounds(centerX - 105, this.height - 40, 100, 20)
                .build());

        // Cancel button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.config.cancel"),
                button -> cancel())
                .bounds(centerX + 5, this.height - 40, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = 50;

        // Title
        Component title = this.title;
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, centerX - titleWidth / 2, 20, 0xFFFFFF, true);

        // Labels
        guiGraphics.drawString(this.font, Component.translatable("gui.promptcraft.config.api_key"),
                centerX - 100, startY - 12, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.promptcraft.config.model"),
                centerX - 100, startY + 23, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.promptcraft.config.base_url"),
                centerX - 100, startY + 58, 0xAAAAAA, false);
    }

    private void toggleConfirmation() {
        config.requireConfirmation = !config.requireConfirmation;
        this.confirmationToggle.setMessage(
                Component.translatable("gui.promptcraft.config.confirmation",
                        config.requireConfirmation ? "ON" : "OFF"));
    }

    private void saveAndClose() {
        config.apiKey = this.apiKeyField.getValue();
        config.modelId = this.modelIdField.getValue();
        config.baseUrl = this.baseUrlField.getValue();

        ConfigManager.saveConfig();

        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void cancel() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
