package com.promptcraft.util;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;

/**
 * Helper class for testing and debugging
 */
public class TestHelper {

    /**
     * Validates the current configuration
     */
    public static boolean validateConfiguration() {
        try {
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();

            if (config == null) {
                PromptCraft.LOGGER.error("Configuration is null");
                return false;
            }

            if (!config.isValid()) {
                PromptCraft.LOGGER.error("Configuration is invalid");
                return false;
            }

            PromptCraft.LOGGER.info("Configuration validation passed");
            return true;

        } catch (Exception e) {
            PromptCraft.LOGGER.error("Error validating configuration", e);
            return false;
        }
    }

    /**
     * Tests the blacklist functionality
     */
    public static boolean testBlacklist() {
        try {
            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();

            // Test with a blacklisted keyword
            String testCommand = "/say This contains delete operation";
            boolean shouldBeBlocked = config.blacklistedKeywords.stream()
                    .anyMatch(keyword -> testCommand.toLowerCase().contains(keyword.toLowerCase()));

            if (shouldBeBlocked) {
                PromptCraft.LOGGER.info("Blacklist test passed - command correctly blocked");
                return true;
            } else {
                PromptCraft.LOGGER.warn("Blacklist test failed - command should have been blocked");
                return false;
            }

        } catch (Exception e) {
            PromptCraft.LOGGER.error("Error testing blacklist", e);
            return false;
        }
    }

    /**
     * Tests command validation
     */
    public static boolean testCommandValidation() {
        try {
            // Test valid commands
            String[] validCommands = {
                    "/say Hello World",
                    "/give @p minecraft:diamond 1",
                    "/tp @p ~ ~10 ~",
                    "/time set day"
            };

            for (String command : validCommands) {
                if (!isValidCommand(command)) {
                    PromptCraft.LOGGER.error("Valid command failed validation: " + command);
                    return false;
                }
            }

            // Test invalid commands
            String[] invalidCommands = {
                    "say Hello", // Missing /
                    "/", // Empty command
                    "", // Empty string
                    null // Null command
            };

            for (String command : invalidCommands) {
                if (isValidCommand(command)) {
                    PromptCraft.LOGGER.error("Invalid command passed validation: " + command);
                    return false;
                }
            }

            PromptCraft.LOGGER.info("Command validation test passed");
            return true;

        } catch (Exception e) {
            PromptCraft.LOGGER.error("Error testing command validation", e);
            return false;
        }
    }

    private static boolean isValidCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        command = command.trim();

        if (!command.startsWith("/")) {
            return false;
        }

        if (command.length() <= 1) {
            return false;
        }

        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        if (command.length() > config.maxCommandLength) {
            return false;
        }

        return true;
    }

    /**
     * Runs all tests
     */
    public static boolean runAllTests() {
        PromptCraft.LOGGER.info("Running PromptCraft tests...");

        boolean configTest = validateConfiguration();
        boolean blacklistTest = testBlacklist();
        boolean validationTest = testCommandValidation();

        boolean allPassed = configTest && blacklistTest && validationTest;

        if (allPassed) {
            PromptCraft.LOGGER.info("All tests passed!");
        } else {
            PromptCraft.LOGGER.error("Some tests failed!");
        }

        return allPassed;
    }

    /**
     * Logs system information for debugging
     */
    public static void logSystemInfo() {
        PromptCraft.LOGGER.info("=== PromptCraft System Information ===");
        PromptCraft.LOGGER.info("Mod Version: 1.0.0");
        PromptCraft.LOGGER.info("Minecraft Version: 1.20.1");
        PromptCraft.LOGGER.info("Java Version: " + System.getProperty("java.version"));
        PromptCraft.LOGGER.info("OS: " + System.getProperty("os.name"));

        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        if (config != null) {
            PromptCraft.LOGGER.info("API Base URL: " + config.baseUrl);
            PromptCraft.LOGGER.info("Model ID: " + config.modelId);
            PromptCraft.LOGGER.info("Language: " + config.language);
            PromptCraft.LOGGER.info("Blacklist Keywords: " + config.blacklistedKeywords.size());
            PromptCraft.LOGGER.info("Command Block Mode: " + config.useCommandBlocks);
            PromptCraft.LOGGER.info("Logging Enabled: " + config.enableLogging);
        }
        PromptCraft.LOGGER.info("=====================================");
    }
}
