package com.promptcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.promptcraft.PromptCraft;
import com.promptcraft.client.gui.PromptCraftScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side initialization for PromptCraft mod (NeoForge)
 */
@OnlyIn(Dist.CLIENT)
public class PromptCraftClient {

    // Key bindings
    private static KeyMapping openGuiKeyBinding;

    public static void init() {
        PromptCraft.LOGGER.info("Initializing PromptCraft client...");

        // Register key bindings through the event bus
        NeoForge.EVENT_BUS.register(ClientEvents.class);

        PromptCraft.LOGGER.info("PromptCraft client initialized successfully!");
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openGuiKeyBinding = new KeyMapping(
                "key.promptcraft.open_gui",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.promptcraft.general");
        event.register(openGuiKeyBinding);
    }

    // Getters for key bindings
    public static KeyMapping getOpenGuiKeyBinding() {
        return openGuiKeyBinding;
    }

    /**
     * Client tick events handler
     */
    @OnlyIn(Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (openGuiKeyBinding != null && openGuiKeyBinding.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new PromptCraftScreen());
                }
            }
        }
    }

    /**
     * Mod bus events for key registration
     */
    @EventBusSubscriber(modid = PromptCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            PromptCraftClient.registerKeyMappings(event);
        }
    }
}
