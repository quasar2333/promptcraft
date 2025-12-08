package com.promptcraft.client.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.network.NetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.List;

/**
 * Handles client-side network packets for Fabric 1.20.x
 */
public class ClientNetworkHandler {

    public static void registerClientHandlers() {
        // Client-side registration (if needed for S2C packets)
        PromptCraft.LOGGER.info("Client network handlers registered");
    }

    /**
     * Sends an execute command packet to the server
     */
    public static void sendExecuteCommand(String command, boolean useCommandBlock) {
        ClientPlayNetworking.send(NetworkHandler.EXECUTE_COMMAND_ID,
                NetworkHandler.createExecuteCommandPacket(command, useCommandBlock));
    }

    /**
     * Sends a blacklist update packet to the server
     */
    public static void sendBlacklistUpdate(List<String> keywords) {
        ClientPlayNetworking.send(NetworkHandler.BLACKLIST_UPDATE_ID,
                NetworkHandler.createBlacklistUpdatePacket(keywords));
    }
}
