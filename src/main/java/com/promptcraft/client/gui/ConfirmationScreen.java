package com.promptcraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Confirmation dialog screen for command execution
 */
@OnlyIn(Dist.CLIENT)
public class ConfirmationScreen extends Screen {
    private final Screen parent;
    private final String command;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmationScreen(Screen parent, String command, Runnable onConfirm, Runnable onCancel) {
        super(Component.translatable("gui.promptcraft.confirm.title"));
        this.parent = parent;
        this.command = command;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Confirm button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.confirm.yes"),
                button -> confirm())
                .bounds(centerX - 105, centerY + 30, 100, 20)
                .build());

        // Cancel button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.confirm.no"),
                button -> cancel())
                .bounds(centerX + 5, centerY + 30, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title
        Component title = Component.translatable("gui.promptcraft.confirm.title");
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, centerX - titleWidth / 2, centerY - 50, 0xFFFFFF, true);

        // Message
        Component message = Component.translatable("gui.promptcraft.confirm.message");
        int messageWidth = this.font.width(message);
        guiGraphics.drawString(this.font, message, centerX - messageWidth / 2, centerY - 30, 0xAAAAAA, true);

        // Command preview
        String displayCommand = command.length() > 50 ? command.substring(0, 47) + "..." : command;
        Component commandText = Component.literal(displayCommand).withStyle(ChatFormatting.YELLOW);
        int commandWidth = this.font.width(commandText);
        guiGraphics.drawString(this.font, commandText, centerX - commandWidth / 2, centerY - 10, 0xFFFFFF, true);
    }

    private void confirm() {
        if (onConfirm != null) {
            onConfirm.run();
        }
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    private void cancel() {
        if (onCancel != null) {
            onCancel.run();
        }
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
