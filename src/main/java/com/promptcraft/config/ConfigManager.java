package com.promptcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.promptcraft.PromptCraft;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages configuration for PromptCraft mod
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("promptcraft");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    
    private static PromptCraftConfig config;
    
    public static void initialize() {
        try {
            // Create config directory if it doesn't exist
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            
            // Load or create config
            loadConfig();
            
        } catch (IOException e) {
            PromptCraft.LOGGER.error("Failed to initialize config", e);
            config = new PromptCraftConfig(); // Use default config
        }
    }
    
    private static void loadConfig() throws IOException {
        if (Files.exists(CONFIG_FILE)) {
            try {
                String json = Files.readString(CONFIG_FILE);
                config = GSON.fromJson(json, PromptCraftConfig.class);
                
                // Validate config
                if (config == null) {
                    config = new PromptCraftConfig();
                }
                
                PromptCraft.LOGGER.info("Config loaded successfully");
            } catch (JsonSyntaxException e) {
                PromptCraft.LOGGER.warn("Invalid config file, creating new one", e);
                config = new PromptCraftConfig();
                saveConfig();
            }
        } else {
            config = new PromptCraftConfig();
            saveConfig();
            PromptCraft.LOGGER.info("Created new config file");
        }
    }
    
    public static void saveConfig() {
        try {
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_FILE, json);
            PromptCraft.LOGGER.debug("Config saved successfully");
        } catch (IOException e) {
            PromptCraft.LOGGER.error("Failed to save config", e);
        }
    }
    
    public static PromptCraftConfig getConfig() {
        return config;
    }
    
    public static void setConfig(PromptCraftConfig newConfig) {
        config = newConfig;
        saveConfig();
    }
    
    /**
     * Configuration class for PromptCraft
     */
    public static class PromptCraftConfig {
        public String apiKey = "";
        public String modelId = "deepseek-ai/DeepSeek-V3";
        public String baseUrl = "https://api.siliconflow.cn/v1";
        public boolean useCommandBlocks = false;
        public List<String> blacklistedKeywords = new ArrayList<>();
        public int maxRetries = 3;
        public int timeoutSeconds = 30;
        public String language = "auto"; // auto, en_us, zh_cn
        public boolean enableLogging = true;
        public boolean requireConfirmation = true;
        public int maxCommandLength = 1000;

        public PromptCraftConfig() {
            // Initialize with default blacklisted keywords
            blacklistedKeywords.add("rm");
            blacklistedKeywords.add("delete");
            blacklistedKeywords.add("format");
            blacklistedKeywords.add("shutdown");
            blacklistedKeywords.add("stop");
            blacklistedKeywords.add("kill");
            blacklistedKeywords.add("destroy");
            blacklistedKeywords.add("clear");
        }

        /**
         * Validates the configuration and returns true if valid
         */
        public boolean isValid() {
            return apiKey != null && !apiKey.trim().isEmpty() &&
                   modelId != null && !modelId.trim().isEmpty() &&
                   baseUrl != null && !baseUrl.trim().isEmpty() &&
                   maxRetries > 0 && timeoutSeconds > 0 &&
                   maxCommandLength > 0;
        }

        /**
         * Gets the effective language setting
         */
        public String getEffectiveLanguage() {
            if ("auto".equals(language)) {
                // Try to detect system language
                String systemLang = System.getProperty("user.language", "en");
                return systemLang.equals("zh") ? "zh_cn" : "en_us";
            }
            return language;
        }
    }
}
