package com.promptcraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.promptcraft.PromptCraft;
import com.promptcraft.client.gui.PromptCraftScreen;
import com.promptcraft.client.network.ClientNetworkHandler;

/**
 * Client-side initialization for PromptCraft mod
 */
@Environment(EnvType.CLIENT)
public class PromptCraftClient implements ClientModInitializer {

    // Key bindings
    private static KeyBinding openGuiKeyBinding;

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
    }

    private void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle main GUI key binding
            while (openGuiKeyBinding.wasPressed()) {
                if (client.currentScreen == null) {
                    // Normal GUI opening
                    client.setScreen(new PromptCraftScreen());
                }
            }
        });
    }

    // Getters for key bindings
    public static KeyBinding getOpenGuiKeyBinding() {
        return openGuiKeyBinding;
    }
}
