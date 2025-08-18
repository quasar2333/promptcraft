package com.promptcraft.client.gui;

import com.promptcraft.client.util.LocalizationHelper;
import com.promptcraft.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Configuration screen for PromptCraft settings
 */
public class ConfigScreen extends Screen {
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 350;
    
    private final Screen parent;
    private ConfigManager.PromptCraftConfig config;
    
    private TextFieldWidget apiKeyField;
    private TextFieldWidget modelIdField;
    private TextFieldWidget baseUrlField;
    private TextFieldWidget timeoutField;
    private TextFieldWidget maxRetriesField;
    private ButtonWidget languageButton;
    private ButtonWidget loggingButton;
    private ButtonWidget confirmationButton;
    
    private int currentLanguageIndex = 0;
    private final String[] languages = {"auto", "en_us", "zh_cn"};
    private final String[] languageNames = {"Auto", "English", "中文"};
    
    public ConfigScreen(Screen parent) {
        super(Text.translatable("gui.promptcraft.config.title"));
        this.parent = parent;
        this.config = new ConfigManager.PromptCraftConfig();
        
        // Copy current config
        ConfigManager.PromptCraftConfig currentConfig = ConfigManager.getConfig();
        this.config.apiKey = currentConfig.apiKey;
        this.config.modelId = currentConfig.modelId;
        this.config.baseUrl = currentConfig.baseUrl;
        this.config.timeoutSeconds = currentConfig.timeoutSeconds;
        this.config.maxRetries = currentConfig.maxRetries;
        this.config.language = currentConfig.language;
        this.config.enableLogging = currentConfig.enableLogging;
        this.config.requireConfirmation = currentConfig.requireConfirmation;
        this.config.blacklistedKeywords.addAll(currentConfig.blacklistedKeywords);
        
        // Find current language index
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(config.language)) {
                currentLanguageIndex = i;
                break;
            }
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;
        
        // API Key field
        Text apiKeyLabel = Text.translatable("gui.promptcraft.config.api_key");
        this.apiKeyField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            startY + 40,
            300,
            20,
            apiKeyLabel
        );
        this.apiKeyField.setMaxLength(200);
        this.apiKeyField.setText(config.apiKey);
        try { this.apiKeyField.setDrawsBackground(false); } catch (Throwable ignored) {}
        this.addDrawableChild(this.apiKeyField);
        this.setInitialFocus(this.apiKeyField);
        
        // Model ID field
        Text modelIdLabel = Text.translatable("gui.promptcraft.config.model_id");
        this.modelIdField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            startY + 80,
            300,
            20,
            modelIdLabel
        );
        this.modelIdField.setMaxLength(100);
        this.modelIdField.setText(config.modelId);
        try { this.modelIdField.setDrawsBackground(false); } catch (Throwable ignored) {}
        this.addDrawableChild(this.modelIdField);
        
        // Base URL field
        Text baseUrlLabel = Text.translatable("gui.promptcraft.config.base_url");
        this.baseUrlField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            startY + 120,
            300,
            20,
            baseUrlLabel
        );
        this.baseUrlField.setMaxLength(200);
        this.baseUrlField.setText(config.baseUrl);
        try { this.baseUrlField.setDrawsBackground(false); } catch (Throwable ignored) {}
        this.addDrawableChild(this.baseUrlField);
        
        // Timeout field
        Text timeoutLabel = Text.translatable("gui.promptcraft.config.timeout");
        this.timeoutField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            startY + 160,
            100,
            20,
            timeoutLabel
        );
        this.timeoutField.setMaxLength(3);
        this.timeoutField.setText(String.valueOf(config.timeoutSeconds));
        try { this.timeoutField.setDrawsBackground(false); } catch (Throwable ignored) {}
        this.addDrawableChild(this.timeoutField);
        
        // Max retries field
        this.maxRetriesField = new TextFieldWidget(
            this.textRenderer,
            centerX - 40,
            startY + 160,
            100,
            20,
            Text.literal("Max Retries")
        );
        this.maxRetriesField.setMaxLength(2);
        this.maxRetriesField.setText(String.valueOf(config.maxRetries));
        try { this.maxRetriesField.setDrawsBackground(false); } catch (Throwable ignored) {}
        this.addDrawableChild(this.maxRetriesField);
        
        // Language button
        this.languageButton = ButtonWidget.builder(
            Text.literal("Language: " + LocalizationHelper.getLanguageDisplayName(languages[currentLanguageIndex])),
            button -> cycleLanguage()
        ).dimensions(centerX - 150, startY + 200, 140, 20).build();
        this.addDrawableChild(this.languageButton);

        // Logging toggle
        this.loggingButton = ButtonWidget.builder(
            Text.literal("Logging: " + LocalizationHelper.getBooleanText(config.enableLogging)),
            button -> toggleLogging()
        ).dimensions(centerX, startY + 200, 140, 20).build();
        this.addDrawableChild(this.loggingButton);

        // Confirmation toggle
        this.confirmationButton = ButtonWidget.builder(
            Text.literal("Confirm: " + LocalizationHelper.getBooleanText(config.requireConfirmation)),
            button -> toggleConfirmation()
        ).dimensions(centerX - 75, startY + 230, 140, 20).build();
        this.addDrawableChild(this.confirmationButton);
        
        // Save button
        ButtonWidget saveButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.config.save"),
            button -> saveConfig()
        ).dimensions(centerX - 100, startY + 280, 80, 20).build();
        this.addDrawableChild(saveButton);
        
        // Cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.config.cancel"),
            button -> this.close()
        ).dimensions(centerX + 20, startY + 280, 80, 20).build();
        this.addDrawableChild(cancelButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Single-layer widgets - render widgets first
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        super.render(context, mouseX, mouseY, delta);

        // Draw all text AFTER widgets to ensure same layer
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, startY - 10, 0xFFFFFF);

        // Field labels
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.promptcraft.config.api_key"),
            centerX - 150, startY + 30, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.promptcraft.config.model_id"),
            centerX - 150, startY + 70, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.promptcraft.config.base_url"),
            centerX - 150, startY + 110, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.promptcraft.config.timeout"),
            centerX - 150, startY + 150, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("Max Retries"),
            centerX - 40, startY + 150, 0xFFFFFF);
    }
    
    private void cycleLanguage() {
        currentLanguageIndex = (currentLanguageIndex + 1) % languages.length;
        config.language = languages[currentLanguageIndex];
        this.languageButton.setMessage(Text.literal("Language: " + LocalizationHelper.getLanguageDisplayName(languages[currentLanguageIndex])));
    }

    private void toggleLogging() {
        config.enableLogging = !config.enableLogging;
        this.loggingButton.setMessage(Text.literal("Logging: " + LocalizationHelper.getBooleanText(config.enableLogging)));
    }

    private void toggleConfirmation() {
        config.requireConfirmation = !config.requireConfirmation;
        this.confirmationButton.setMessage(Text.literal("Confirm: " + LocalizationHelper.getBooleanText(config.requireConfirmation)));
    }
    
    private void saveConfig() {
        // Update config with field values
        config.apiKey = this.apiKeyField.getText().trim();
        config.modelId = this.modelIdField.getText().trim();
        config.baseUrl = this.baseUrlField.getText().trim();
        
        try {
            config.timeoutSeconds = Integer.parseInt(this.timeoutField.getText());
            config.maxRetries = Integer.parseInt(this.maxRetriesField.getText());
        } catch (NumberFormatException e) {
            // Show error message
            return;
        }
        
        // Validate config
        if (!config.isValid()) {
            // Show validation error
            return;
        }
        
        // Save config
        ConfigManager.setConfig(config);
        
        // Close screen
        this.close();
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
