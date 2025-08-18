package com.promptcraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import com.promptcraft.PromptCraft;
import com.promptcraft.client.gui.PromptCraftScreen;
import com.promptcraft.client.network.ClientNetworkHandler;
import com.promptcraft.config.ConfigManager;

/**
 * Client-side initialization for PromptCraft mod
 */
@Environment(EnvType.CLIENT)
public class PromptCraftClient implements ClientModInitializer {
    
    // Key bindings
    private static KeyBinding openGuiKeyBinding;
    private static KeyBinding quickGenerateKeyBinding;
    private static KeyBinding toggleModeKeyBinding;
    
    @Override
    public void onInitializeClient() {
        PromptCraft.LOGGER.info("Initializing PromptCraft client...");
        
        // Register key bindings
        registerKeyBindings();
        
        // Register client tick events
        registerClientEvents();
        
        // Register client network handlers
        ClientNetworkHandler.registerClientHandlers();
        
        PromptCraft.LOGGER.info("PromptCraft client initialized successfully!");
    }
    
    private void registerKeyBindings() {
        // Main GUI key binding
        openGuiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.promptcraft.open_gui", // Translation key
            InputUtil.Type.KEYSYM, // Input type
            GLFW.GLFW_KEY_G, // Default key (G)
            "category.promptcraft.general" // Category
        ));

        // Quick generate key binding (Ctrl+G)
        quickGenerateKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.promptcraft.quick_generate",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // Same key but with modifier
            "category.promptcraft.general"
        ));

        // Toggle command block mode (Alt+G)
        toggleModeKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.promptcraft.toggle_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // Same key but with modifier
            "category.promptcraft.general"
        ));
    }
    
    private void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle main GUI key binding
            while (openGuiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    // Check for modifiers
                    boolean ctrlPressed = Screen.hasControlDown();
                    boolean altPressed = Screen.hasAltDown();

                    if (ctrlPressed) {
                        // Quick generate mode - open with last input
                        openQuickGenerateMode(client);
                    } else if (altPressed) {
                        // Toggle command block mode
                        toggleCommandBlockMode(client);
                    } else {
                        // Normal GUI opening
                        client.setScreen(new PromptCraftScreen());
                    }
                }
            }
        });
    }
    
    private void openQuickGenerateMode(net.minecraft.client.MinecraftClient client) {
        // TODO: Implement quick generate mode
        // For now, just open normal GUI
        client.setScreen(new PromptCraftScreen());

        if (client.player != null) {
            client.player.sendMessage(Text.literal("Quick Generate Mode (Ctrl+G)").formatted(Formatting.YELLOW), false);
        }
    }

    private void toggleCommandBlockMode(net.minecraft.client.MinecraftClient client) {
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        config.useCommandBlocks = !config.useCommandBlocks;
        ConfigManager.saveConfig();

        if (client.player != null) {
            String mode = config.useCommandBlocks ? "Command Block" : "Normal";
            client.player.sendMessage(
                Text.literal("Command mode switched to: " + mode).formatted(Formatting.GREEN),
                false
            );
        }
    }

    // Getters for key bindings
    public static KeyBinding getOpenGuiKeyBinding() {
        return openGuiKeyBinding;
    }

    public static KeyBinding getQuickGenerateKeyBinding() {
        return quickGenerateKeyBinding;
    }

    public static KeyBinding getToggleModeKeyBinding() {
        return toggleModeKeyBinding;
    }
}
