package com.promptcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.promptcraft.PromptCraft;
import com.promptcraft.client.gui.PromptCraftScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side initialization for PromptCraft mod (Forge 1.20.1)
 */
@OnlyIn(Dist.CLIENT)
public class PromptCraftClient {

    private static KeyMapping openGuiKeyBinding;

    public static void init() {
        PromptCraft.LOGGER.info("Initializing PromptCraft client...");

        // Register key bindings through the mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PromptCraftClient::registerKeyMappings);

        // Register client tick events
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);

        PromptCraft.LOGGER.info("PromptCraft client initialized successfully!");
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openGuiKeyBinding = new KeyMapping(
                "key.promptcraft.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.promptcraft.general");
        event.register(openGuiKeyBinding);
    }

    public static KeyMapping getOpenGuiKeyBinding() {
        return openGuiKeyBinding;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (openGuiKeyBinding != null && openGuiKeyBinding.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new PromptCraftScreen());
                }
            }
        }
    }
}
