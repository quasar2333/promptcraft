package com.promptcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.promptcraft.config.ConfigManager;
import com.promptcraft.network.NetworkHandler;
import com.promptcraft.util.TestHelper;

/**
 * PromptCraft - AI-powered command generation for Minecraft
 * Main mod initializer for server-side functionality (Fabric 1.20.x)
 */
public class PromptCraft implements ModInitializer {
    public static final String MOD_ID = "promptcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Network packet identifiers
    public static final Identifier EXECUTE_COMMAND_PACKET = Identifier.of(MOD_ID, "execute_command");
    public static final Identifier CONFIG_SYNC_PACKET = Identifier.of(MOD_ID, "config_sync");
    public static final Identifier BLACKLIST_UPDATE_PACKET = Identifier.of(MOD_ID, "blacklist_update");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PromptCraft mod...");

        // Initialize configuration system
        ConfigManager.initialize();

        // Register network handlers
        NetworkHandler.register();

        // Log system information
        TestHelper.logSystemInfo();

        // Run tests in development environment
        if (Boolean.getBoolean("promptcraft.runTests")) {
            TestHelper.runAllTests();
        }

        LOGGER.info("PromptCraft mod initialized successfully!");
    }

    /**
     * Creates an identifier with the mod's namespace
     */
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
