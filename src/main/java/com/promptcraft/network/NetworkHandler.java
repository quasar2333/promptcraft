package com.promptcraft.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.util.ErrorHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Handles server-side network packets
 */
public class NetworkHandler {

    // Custom payload for command execution
    public record ExecuteCommandPayload(String command, boolean useCommandBlock) implements CustomPayload {
        public static final CustomPayload.Id<ExecuteCommandPayload> ID = new CustomPayload.Id<>(
                PromptCraft.EXECUTE_COMMAND_PACKET);
        public static final PacketCodec<RegistryByteBuf, ExecuteCommandPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ExecuteCommandPayload::command,
                PacketCodecs.BOOLEAN, ExecuteCommandPayload::useCommandBlock,
                ExecuteCommandPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // Custom payload for blacklist updates
    public record BlacklistUpdatePayload(java.util.List<String> keywords) implements CustomPayload {
        public static final CustomPayload.Id<BlacklistUpdatePayload> ID = new CustomPayload.Id<>(
                PromptCraft.BLACKLIST_UPDATE_PACKET);
        public static final PacketCodec<RegistryByteBuf, BlacklistUpdatePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING.collect(PacketCodecs.toList()), BlacklistUpdatePayload::keywords,
                BlacklistUpdatePayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerServerHandlers() {
        // Register payload types
        PayloadTypeRegistry.playC2S().register(ExecuteCommandPayload.ID, ExecuteCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BlacklistUpdatePayload.ID, BlacklistUpdatePayload.CODEC);

        // Handle command execution requests from client
        ServerPlayNetworking.registerGlobalReceiver(ExecuteCommandPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                executeCommand(context.player(), payload.command(), payload.useCommandBlock());
            });
        });

        // Handle blacklist update requests
        ServerPlayNetworking.registerGlobalReceiver(BlacklistUpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Only allow ops to update blacklist in multiplayer
            if (context.server().isSingleplayer() || player.hasPermissionLevel(2)) {
                ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
                config.blacklistedKeywords.clear();
                config.blacklistedKeywords.addAll(payload.keywords());

                ConfigManager.saveConfig();
                PromptCraft.LOGGER.info("Blacklist updated by player: " + player.getName().getString());
            }
        });
    }

    private static void executeCommand(ServerPlayerEntity player, String command, boolean useCommandBlock) {
        try {
            // Validate command format
            if (!isValidCommandFormat(command)) {
                player.sendMessage(Text.translatable("promptcraft.command.invalid_format"), false);
                return;
            }

            // Check blacklist
            if (isCommandBlacklisted(command)) {
                player.sendMessage(Text.translatable("promptcraft.command.blacklisted"), false);
                return;
            }

            // Check permissions for command block mode
            if (useCommandBlock && !hasCommandBlockPermission(player)) {
                player.sendMessage(Text.translatable("promptcraft.command.no_permission"), false);
                return;
            }

            // Only check blacklist - remove other safety restrictions

            // Execute command
            if (useCommandBlock) {
                // Execute as command block (higher permission level)
                player.getServer().getCommandManager().executeWithPrefix(
                        player.getServer().getCommandSource().withLevel(2), command);
            } else {
                // Execute as player
                player.getServer().getCommandManager().executeWithPrefix(
                        player.getCommandSource(), command);
            }

            // Log successful execution
            PromptCraft.LOGGER.info("Command executed by {} ({}): {}",
                    player.getName().getString(),
                    useCommandBlock ? "Command Block" : "Player",
                    command);

            // Send success message to player
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

        // Must start with /
        if (!command.startsWith("/")) {
            return false;
        }

        // Must have at least one character after /
        if (command.length() <= 1) {
            return false;
        }

        // Check for reasonable length
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
        // In single player, always allow
        if (player.getServer().isSingleplayer()) {
            return true;
        }

        // In multiplayer, require op level 2 or higher
        return player.hasPermissionLevel(2);
    }

}
