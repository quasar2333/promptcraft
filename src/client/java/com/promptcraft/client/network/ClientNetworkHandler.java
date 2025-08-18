package com.promptcraft.client.network;

import com.promptcraft.PromptCraft;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Handles client-side network packets
 */
public class ClientNetworkHandler {

    // Config sync payload
    public record ConfigSyncPayload() implements CustomPayload {
        public static final CustomPayload.Id<ConfigSyncPayload> ID = new CustomPayload.Id<>(PromptCraft.CONFIG_SYNC_PACKET);
        public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC = PacketCodec.unit(new ConfigSyncPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerClientHandlers() {
        // Register payload type
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);

        // Handle config sync from server
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, context) -> {
            // Handle server config sync if needed
            PromptCraft.LOGGER.debug("Received config sync from server");
        });
    }
}
