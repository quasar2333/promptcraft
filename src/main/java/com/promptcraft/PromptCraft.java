package com.promptcraft;

import com.mojang.logging.LogUtils;
import com.promptcraft.client.PromptCraftClient;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * PromptCraft - AI-powered command generation for Minecraft
 * Main mod initializer for NeoForge
 */
@Mod(PromptCraft.MOD_ID)
public class PromptCraft {
    public static final String MOD_ID = "promptcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PromptCraft(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing PromptCraft mod...");

        // Register mod lifecycle events
        modEventBus.addListener(this::commonSetup);

        // Register network handlers
        NetworkHandler.register(modEventBus);

        LOGGER.info("PromptCraft mod constructor completed!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("PromptCraft common setup...");

        // Initialize configuration system
        event.enqueueWork(() -> {
            ConfigManager.initialize();
        });

        LOGGER.info("PromptCraft common setup completed!");
    }

    /**
     * Creates a ResourceLocation with the mod's namespace
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Client-side event subscriber
     */
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("PromptCraft client setup...");
            PromptCraftClient.init();
            LOGGER.info("PromptCraft client setup completed!");
        }
    }
}
