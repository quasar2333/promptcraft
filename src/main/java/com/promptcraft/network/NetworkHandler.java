package com.promptcraft.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.util.ErrorHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles server-side network packets for Fabric 1.20.x
 */
public class NetworkHandler {

    // Packet identifiers
    public static final Identifier EXECUTE_COMMAND_ID = Identifier.of(PromptCraft.MOD_ID, "execute_command");
    public static final Identifier BLACKLIST_UPDATE_ID = Identifier.of(PromptCraft.MOD_ID, "blacklist_update");

    /**
     * Registers all server-side packet handlers
     */
    public static void register() {
        // Register execute command packet handler
        ServerPlayNetworking.registerGlobalReceiver(EXECUTE_COMMAND_ID,
                (server, player, handler, buf, responseSender) -> {
                    String command = buf.readString();
                    boolean useCommandBlock = buf.readBoolean();
                    server.execute(() -> executeCommand(player, command, useCommandBlock));
                });

        // Register blacklist update packet handler
        ServerPlayNetworking.registerGlobalReceiver(BLACKLIST_UPDATE_ID,
                (server, player, handler, buf, responseSender) -> {
                    int size = buf.readInt();
                    List<String> keywords = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        keywords.add(buf.readString());
                    }
                    server.execute(() -> {
                        if (server.isSingleplayer() || player.hasPermissionLevel(2)) {
                            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
                            config.blacklistedKeywords.clear();
                            config.blacklistedKeywords.addAll(keywords);
                            ConfigManager.saveConfig();
                            PromptCraft.LOGGER.info("Blacklist updated by player: " + player.getName().getString());
                        }
                    });
                });

        PromptCraft.LOGGER.info("Network handlers registered");
    }

    /**
     * Creates a packet buffer for execute command
     */
    public static PacketByteBuf createExecuteCommandPacket(String command, boolean useCommandBlock) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(command);
        buf.writeBoolean(useCommandBlock);
        return buf;
    }

    /**
     * Creates a packet buffer for blacklist update
     */
    public static PacketByteBuf createBlacklistUpdatePacket(List<String> keywords) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(keywords.size());
        for (String keyword : keywords) {
            buf.writeString(keyword);
        }
        return buf;
    }

    // ==================== Command Execution ====================

    private static void executeCommand(ServerPlayerEntity player, String command, boolean useCommandBlock) {
        try {
            if (!isValidCommandFormat(command)) {
                player.sendMessage(Text.translatable("promptcraft.command.invalid_format"), false);
                return;
            }

            if (isCommandBlacklisted(command)) {
                player.sendMessage(Text.translatable("promptcraft.command.blacklisted"), false);
                return;
            }

            if (useCommandBlock && !hasCommandBlockPermission(player)) {
                player.sendMessage(Text.translatable("promptcraft.command.no_permission"), false);
                return;
            }

            // Execute the command
            if (useCommandBlock) {
                player.getServer().getCommandManager().executeWithPrefix(
                        player.getServer().getCommandSource().withLevel(2), command);
            } else {
                player.getServer().getCommandManager().executeWithPrefix(
                        player.getCommandSource(), command);
            }

            PromptCraft.LOGGER.info("Command executed by {} ({}): {}",
                    player.getName().getString(),
                    useCommandBlock ? "Command Block" : "Player",
                    command);

            player.sendMessage(Text.translatable("promptcraft.command.success"), false);

        } catch (Exception e) {
            ErrorHandler.handleCommandError(command, e, player);
        }
    }

    private static boolean isValidCommandFormat(String command) {
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

    private static boolean isCommandBlacklisted(String command) {
        String lowerCommand = command.toLowerCase();
        return ConfigManager.getConfig().blacklistedKeywords.stream()
                .anyMatch(keyword -> lowerCommand.contains(keyword.toLowerCase()));
    }

    private static boolean hasCommandBlockPermission(ServerPlayerEntity player) {
        if (player.getServer().isSingleplayer()) {
            return true;
        }
        return player.hasPermissionLevel(2);
    }
}
