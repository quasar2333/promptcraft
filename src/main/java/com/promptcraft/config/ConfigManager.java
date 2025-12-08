package com.promptcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.promptcraft.PromptCraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages configuration for PromptCraft mod (Forge 1.20.1)
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("promptcraft");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Path CONFIG_BACKUP = CONFIG_DIR.resolve("config.json.backup");

    private static volatile PromptCraftConfig config;
    private static final Object CONFIG_LOCK = new Object();

    public static void initialize() {
        synchronized (CONFIG_LOCK) {
            try {
                if (!Files.exists(CONFIG_DIR)) {
                    Files.createDirectories(CONFIG_DIR);
                }
                loadConfig();
            } catch (IOException e) {
                PromptCraft.LOGGER.error("Failed to initialize config", e);
                config = new PromptCraftConfig();
            }
        }
    }

    private static void loadConfig() throws IOException {
        if (Files.exists(CONFIG_FILE)) {
            try {
                String json = Files.readString(CONFIG_FILE);
                config = GSON.fromJson(json, PromptCraftConfig.class);

                if (config == null) {
                    config = new PromptCraftConfig();
                }

                if (config.blacklistedKeywords == null) {
                    config.blacklistedKeywords = new ArrayList<>();
                }

                PromptCraft.LOGGER.info("Config loaded successfully");
            } catch (JsonSyntaxException e) {
                PromptCraft.LOGGER.warn("Invalid config file, attempting to restore from backup", e);

                if (Files.exists(CONFIG_BACKUP)) {
                    try {
                        String backupJson = Files.readString(CONFIG_BACKUP);
                        config = GSON.fromJson(backupJson, PromptCraftConfig.class);
                        if (config != null) {
                            PromptCraft.LOGGER.info("Config restored from backup successfully");
                            saveConfig();
                            return;
                        }
                    } catch (Exception backupError) {
                        PromptCraft.LOGGER.warn("Failed to restore from backup", backupError);
                    }
                }

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
        synchronized (CONFIG_LOCK) {
            try {
                if (Files.exists(CONFIG_FILE)) {
                    try {
                        Files.copy(CONFIG_FILE, CONFIG_BACKUP,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException backupError) {
                        PromptCraft.LOGGER.warn("Failed to create config backup", backupError);
                    }
                }

                String json = GSON.toJson(config);
                Files.writeString(CONFIG_FILE, json);
                PromptCraft.LOGGER.debug("Config saved successfully");
            } catch (IOException e) {
                PromptCraft.LOGGER.error("Failed to save config", e);
            }
        }
    }

    public static PromptCraftConfig getConfig() {
        if (config == null) {
            synchronized (CONFIG_LOCK) {
                if (config == null) {
                    initialize();
                }
            }
        }
        return config;
    }

    public static void setConfig(PromptCraftConfig newConfig) {
        synchronized (CONFIG_LOCK) {
            config = newConfig;
            saveConfig();
        }
    }

    public static class PromptCraftConfig {
        public String apiKey = "";
        public String modelId = "deepseek-ai/DeepSeek-V3";
        public String baseUrl = "https://api.siliconflow.cn/v1";
        public boolean useCommandBlocks = false;
        public List<String> blacklistedKeywords = new ArrayList<>();
        public int maxRetries = 3;
        public int timeoutSeconds = 30;
        public String language = "auto";
        public boolean enableLogging = true;
        public boolean requireConfirmation = true;
        public int maxCommandLength = 1000;

        public PromptCraftConfig() {
        }

        public boolean isValid() {
            return apiKey != null && !apiKey.trim().isEmpty() &&
                    modelId != null && !modelId.trim().isEmpty() &&
                    baseUrl != null && !baseUrl.trim().isEmpty() &&
                    maxRetries > 0 && timeoutSeconds > 0 &&
                    maxCommandLength > 0;
        }

        public String getEffectiveLanguage() {
            if ("auto".equals(language)) {
                String systemLang = System.getProperty("user.language", "en");
                return systemLang.equals("zh") ? "zh_cn" : "en_us";
            }
            return language;
        }
    }
}
