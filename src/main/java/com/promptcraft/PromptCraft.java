package com.promptcraft;

import com.mojang.logging.LogUtils;
import com.promptcraft.client.PromptCraftClient;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * PromptCraft - AI-powered command generation for Minecraft
 * Main mod initializer for Forge 1.20.1
 */
@Mod(PromptCraft.MOD_ID)
public class PromptCraft {
    public static final String MOD_ID = "promptcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PromptCraft() {
        LOGGER.info("Initializing PromptCraft mod...");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register mod lifecycle events
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Register network handlers
        NetworkHandler.register();

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("PromptCraft mod constructor completed!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("PromptCraft common setup...");

        // Initialize configuration system
        event.enqueueWork(ConfigManager::initialize);

        LOGGER.info("PromptCraft common setup completed!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("PromptCraft client setup...");
        PromptCraftClient.init();
        LOGGER.info("PromptCraft client setup completed!");
    }

    /**
     * Creates a ResourceLocation with the mod's namespace
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
