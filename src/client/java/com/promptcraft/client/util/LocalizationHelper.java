package com.promptcraft.client.util;

import com.promptcraft.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Helper class for localization and language management
 */
public class LocalizationHelper {
    
    /**
     * Gets a localized text with the current language setting
     */
    public static Text getText(String key, Object... args) {
        return Text.translatable(key, args);
    }
    
    /**
     * Gets the current language code
     */
    public static String getCurrentLanguage() {
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        return config.getEffectiveLanguage();
    }
    
    /**
     * Checks if the current language is Chinese
     */
    public static boolean isChineseLanguage() {
        String lang = getCurrentLanguage();
        return lang.startsWith("zh");
    }
    
    /**
     * Gets localized text for boolean values (ON/OFF, 是/否)
     */
    public static String getBooleanText(boolean value) {
        if (isChineseLanguage()) {
            return value ? "开" : "关";
        } else {
            return value ? "ON" : "OFF";
        }
    }
    
    /**
     * Gets localized text for yes/no values
     */
    public static String getYesNoText(boolean value) {
        if (isChineseLanguage()) {
            return value ? "是" : "否";
        } else {
            return value ? "Yes" : "No";
        }
    }
    
    /**
     * Gets the display name for a language code
     */
    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "auto":
                return isChineseLanguage() ? "自动" : "Auto";
            case "en_us":
                return "English";
            case "zh_cn":
                return "中文";
            default:
                return languageCode;
        }
    }
    
    /**
     * Formats a message with proper localization
     */
    public static Text formatMessage(String key, Object... args) {
        return Text.translatable(key, args);
    }
    
    /**
     * Gets the client's current language setting
     */
    public static String getClientLanguage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getLanguageManager() != null) {
            return client.getLanguageManager().getLanguage();
        }
        return "en_us";
    }
    
    /**
     * Checks if a language is supported by the mod
     */
    public static boolean isLanguageSupported(String languageCode) {
        return "en_us".equals(languageCode) || "zh_cn".equals(languageCode);
    }
    
    /**
     * Gets the fallback language if the current one is not supported
     */
    public static String getFallbackLanguage(String languageCode) {
        if (isLanguageSupported(languageCode)) {
            return languageCode;
        }
        
        // Try to match by language family
        if (languageCode.startsWith("zh")) {
            return "zh_cn";
        }
        
        // Default to English
        return "en_us";
    }
}
