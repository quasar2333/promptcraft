package com.promptcraft.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.util.ErrorHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Handles server-side network packets for Forge 1.20.1
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PromptCraft.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, ExecuteCommandPacket.class,
                ExecuteCommandPacket::encode, ExecuteCommandPacket::decode,
                ExecuteCommandPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(packetId++, BlacklistUpdatePacket.class,
                BlacklistUpdatePacket::encode, BlacklistUpdatePacket::decode,
                BlacklistUpdatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        PromptCraft.LOGGER.info("Network handlers registered");
    }

    // Send command to server
    public static void sendExecuteCommand(String command, boolean useCommandBlock) {
        INSTANCE.sendToServer(new ExecuteCommandPacket(command, useCommandBlock));
    }

    // Send blacklist update to server
    public static void sendBlacklistUpdate(List<String> keywords) {
        INSTANCE.sendToServer(new BlacklistUpdatePacket(keywords));
    }

    // ==================== Packet Classes ====================

    /**
     * Packet for command execution
     */
    public static class ExecuteCommandPacket {
        private final String command;
        private final boolean useCommandBlock;

        public ExecuteCommandPacket(String command, boolean useCommandBlock) {
            this.command = command;
            this.useCommandBlock = useCommandBlock;
        }

        public static void encode(ExecuteCommandPacket packet, FriendlyByteBuf buf) {
            buf.writeUtf(packet.command);
            buf.writeBoolean(packet.useCommandBlock);
        }

        public static ExecuteCommandPacket decode(FriendlyByteBuf buf) {
            return new ExecuteCommandPacket(buf.readUtf(), buf.readBoolean());
        }

        public static void handle(ExecuteCommandPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    executeCommand(player, packet.command, packet.useCommandBlock);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    /**
     * Packet for blacklist updates
     */
    public static class BlacklistUpdatePacket {
        private final List<String> keywords;

        public BlacklistUpdatePacket(List<String> keywords) {
            this.keywords = keywords;
        }

        public static void encode(BlacklistUpdatePacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.keywords.size());
            for (String keyword : packet.keywords) {
                buf.writeUtf(keyword);
            }
        }

        public static BlacklistUpdatePacket decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<String> keywords = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                keywords.add(buf.readUtf());
            }
            return new BlacklistUpdatePacket(keywords);
        }

        public static void handle(BlacklistUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    if (player.getServer().isSingleplayer() || player.hasPermissions(2)) {
                        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
                        config.blacklistedKeywords.clear();
                        config.blacklistedKeywords.addAll(packet.keywords);
                        ConfigManager.saveConfig();
                        PromptCraft.LOGGER.info("Blacklist updated by player: " + player.getName().getString());
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // ==================== Command Execution ====================

    private static void executeCommand(ServerPlayer player, String command, boolean useCommandBlock) {
        try {
            if (!isValidCommandFormat(command)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.invalid_format"));
                return;
            }

            if (isCommandBlacklisted(command)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.blacklisted"));
                return;
            }

            if (useCommandBlock && !hasCommandBlockPermission(player)) {
                player.sendSystemMessage(Component.translatable("promptcraft.command.no_permission"));
                return;
            }

            if (useCommandBlock) {
                player.getServer().getCommands().performPrefixedCommand(
                        player.getServer().createCommandSourceStack().withPermission(2), command);
            } else {
                player.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack(), command);
            }

            PromptCraft.LOGGER.info("Command executed by {} ({}): {}",
                    player.getName().getString(),
                    useCommandBlock ? "Command Block" : "Player",
                    command);

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

    private static boolean hasCommandBlockPermission(ServerPlayer player) {
        if (player.getServer().isSingleplayer()) {
            return true;
        }
        return player.hasPermissions(2);
    }
}
