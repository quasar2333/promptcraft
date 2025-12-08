package com.promptcraft.network;

import com.promptcraft.PromptCraft;
import com.promptcraft.config.ConfigManager;
import com.promptcraft.util.ErrorHandler;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles server-side network packets for Fabric 1.20.x
 */
public class NetworkHandler {

    // Packet types
    public static final PacketType<ExecuteCommandPacket> EXECUTE_COMMAND_TYPE = PacketType
            .create(new Identifier(PromptCraft.MOD_ID, "execute_command"), ExecuteCommandPacket::new);
    public static final PacketType<BlacklistUpdatePacket> BLACKLIST_UPDATE_TYPE = PacketType
            .create(new Identifier(PromptCraft.MOD_ID, "blacklist_update"), BlacklistUpdatePacket::new);

    // Execute command packet
    public record ExecuteCommandPacket(String command, boolean useCommandBlock) implements FabricPacket {
        public ExecuteCommandPacket(PacketByteBuf buf) {
            this(buf.readString(), buf.readBoolean());
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeString(command);
            buf.writeBoolean(useCommandBlock);
        }

        @Override
        public PacketType<?> getType() {
            return EXECUTE_COMMAND_TYPE;
        }
    }

    // Blacklist update packet
    public record BlacklistUpdatePacket(List<String> keywords) implements FabricPacket {
        public BlacklistUpdatePacket(PacketByteBuf buf) {
            this(readStringList(buf));
        }

        private static List<String> readStringList(PacketByteBuf buf) {
            int size = buf.readInt();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(buf.readString());
            }
            return list;
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeInt(keywords.size());
            for (String keyword : keywords) {
                buf.writeString(keyword);
            }
        }

        @Override
        public PacketType<?> getType() {
            return BLACKLIST_UPDATE_TYPE;
        }
    }

    /**
     * Registers all server-side packet handlers
     */
    public static void register() {
        // Register execute command packet handler
        ServerPlayNetworking.registerGlobalReceiver(EXECUTE_COMMAND_TYPE,
                (packet, player, responseSender) -> {
                    player.getServer()
                            .execute(() -> executeCommand(player, packet.command(), packet.useCommandBlock()));
                });

        // Register blacklist update packet handler
        ServerPlayNetworking.registerGlobalReceiver(BLACKLIST_UPDATE_TYPE,
                (packet, player, responseSender) -> {
                    player.getServer().execute(() -> {
                        if (player.getServer().isSingleplayer() || player.hasPermissionLevel(2)) {
                            ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
                            config.blacklistedKeywords.clear();
                            config.blacklistedKeywords.addAll(packet.keywords());
                            ConfigManager.saveConfig();
                            PromptCraft.LOGGER.info("Blacklist updated by player: " + player.getName().getString());
                        }
                    });
                });

        PromptCraft.LOGGER.info("Network handlers registered");
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
