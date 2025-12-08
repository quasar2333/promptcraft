package com.promptcraft.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.util.ErrorHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

/**
 * Handles server-side network packets for NeoForge
 */
public class NetworkHandler {
    public static final ResourceLocation EXECUTE_COMMAND_ID = PromptCraft.id("execute_command");
    public static final ResourceLocation BLACKLIST_UPDATE_ID = PromptCraft.id("blacklist_update");

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Register execute command packet (Client -> Server)
        registrar.playToServer(
                ExecuteCommandPayload.TYPE,
                ExecuteCommandPayload.STREAM_CODEC,
                NetworkHandler::handleExecuteCommand);

        // Register blacklist update packet (Client -> Server)
        registrar.playToServer(
                BlacklistUpdatePayload.TYPE,
                BlacklistUpdatePayload.STREAM_CODEC,
                NetworkHandler::handleBlacklistUpdate);

        PromptCraft.LOGGER.info("Network handlers registered");
    }

    // Handle command execution
    private static void handleExecuteCommand(final ExecuteCommandPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                executeCommand(player, payload.command(), payload.useCommandBlock());
            }
        });
    }

    // Handle blacklist update
    private static void handleBlacklistUpdate(final BlacklistUpdatePayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Only allow ops to update blacklist in multiplayer
                if (player.getServer().isSingleplayer() || player.hasPermissions(2)) {
                    ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
                    config.blacklistedKeywords.clear();
                    config.blacklistedKeywords.addAll(payload.keywords());

                    ConfigManager.saveConfig();
                    PromptCraft.LOGGER.info("Blacklist updated by player: " + player.getName().getString());
                }
            }
        });
    }

    private static void executeCommand(ServerPlayer player, String command, boolean useCommandBlock) {
        try {
            // Validate command format
            if (!isValidCommandFormat(command)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.invalid_format"));
                return;
            }

            // Check blacklist
            if (isCommandBlacklisted(command)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.blacklisted"));
                return;
            }

            // Check permissions for command block mode
            if (useCommandBlock && !hasCommandBlockPermission(player)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.no_permission"));
                return;
            }

            // Execute command
            if (useCommandBlock) {
                // Execute as command block (higher permission level)
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack().withPermission(2), command);
            } else {
                // Execute as player
                player.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack(), command);
            }

            // Log successful execution
            PromptCraft.LOGGER.info("Command executed by {} ({}): {}",
                    player.getName().getString(),
                    useCommandBlock ? "Command Block" : "Player",
                    command);

            // Send success message to player
            player.sendSystemMessage(Component.translatable("promptcraft.command.success"));

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

    private static boolean hasCommandBlockPermission(ServerPlayer player) {
        // In single player, always allow
        if (player.getServer().isSingleplayer()) {
            return true;
        }

        // In multiplayer, require op level 2 or higher
        return player.hasPermissions(2);
    }

    // Send command to server
    public static void sendExecuteCommand(String command, boolean useCommandBlock) {
        PacketDistributor.sendToServer(new ExecuteCommandPayload(command, useCommandBlock));
    }

    // Send blacklist update to server
    public static void sendBlacklistUpdate(List<String> keywords) {
        PacketDistributor.sendToServer(new BlacklistUpdatePayload(keywords));
    }

    // ==================== Payload Records ====================

    /**
     * Payload for command execution
     */
    public record ExecuteCommandPayload(String command, boolean useCommandBlock) implements CustomPacketPayload {
        public static final Type<ExecuteCommandPayload> TYPE = new Type<>(EXECUTE_COMMAND_ID);

        public static final StreamCodec<FriendlyByteBuf, ExecuteCommandPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ExecuteCommandPayload::command,
                ByteBufCodecs.BOOL, ExecuteCommandPayload::useCommandBlock,
                ExecuteCommandPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Payload for blacklist updates
     */
    public record BlacklistUpdatePayload(List<String> keywords) implements CustomPacketPayload {
        public static final Type<BlacklistUpdatePayload> TYPE = new Type<>(BLACKLIST_UPDATE_ID);

        public static final StreamCodec<FriendlyByteBuf, BlacklistUpdatePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), BlacklistUpdatePayload::keywords,
                BlacklistUpdatePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
