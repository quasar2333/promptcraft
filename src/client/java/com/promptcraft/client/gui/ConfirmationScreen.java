package com.promptcraft.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Confirmation dialog for command execution
 */
public class ConfirmationScreen extends Screen {
    private static final int WINDOW_WIDTH = 350;
    private static final int WINDOW_HEIGHT = 150;
    
    private final Screen parent;
    private final String command;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    
    public ConfirmationScreen(Screen parent, String command, Runnable onConfirm, Runnable onCancel) {
        super(Text.translatable("gui.promptcraft.confirm.title"));
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
        int startY = centerY - WINDOW_HEIGHT / 2;
        
        // Confirm button
        ButtonWidget confirmButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.confirm.yes").formatted(Formatting.GREEN),
            button -> {
                if (onConfirm != null) {
                    onConfirm.run();
                }
                this.close();
            }
        ).dimensions(centerX - 80, startY + 100, 70, 20).build();
        this.addDrawableChild(confirmButton);
        
        // Cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
            Text.translatable("gui.promptcraft.confirm.no").formatted(Formatting.RED),
            button -> {
                if (onCancel != null) {
                    onCancel.run();
                }
                this.close();
            }
        ).dimensions(centerX + 10, startY + 100, 70, 20).build();
        this.addDrawableChild(cancelButton);
        
        this.setInitialFocus(cancelButton); // Default to cancel for safety
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Single widget layer - render widgets first
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        super.render(context, mouseX, mouseY, delta);

        // Draw all text AFTER widgets to ensure same layer
        // Title and message
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, startY - 10, 0xFFFFFF);
        Text message = Text.translatable("gui.promptcraft.confirm.message");
        context.drawCenteredTextWithShadow(this.textRenderer, message, centerX, startY + 20, 0xFFFFFF);

        // Command preview (same layer as widgets)
        drawWrappedCommand(context, command, centerX - WINDOW_WIDTH/2 + 10, startY + 40, WINDOW_WIDTH - 20);
    }
    
    private void drawWrappedCommand(DrawContext context, String command, int x, int y, int maxWidth) {
        // Split command into words for wrapping
        String[] words = command.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = y;
        int lineHeight = this.textRenderer.fontHeight + 2;
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (this.textRenderer.getWidth(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                if (currentLine.length() > 0) {
                    context.drawTextWithShadow(this.textRenderer, 
                        Text.literal(currentLine.toString()).formatted(Formatting.YELLOW), 
                        x, currentY, 0xFFFFFF);
                    currentY += lineHeight;
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            context.drawTextWithShadow(this.textRenderer, 
                Text.literal(currentLine.toString()).formatted(Formatting.YELLOW), 
                x, currentY, 0xFFFFFF);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC key cancels
        if (keyCode == 256) { // GLFW.GLFW_KEY_ESCAPE
            if (onCancel != null) {
                onCancel.run();
            }
            this.close();
            return true;
        }
        
        // Enter key confirms
        if (keyCode == 257) { // GLFW.GLFW_KEY_ENTER
            if (onConfirm != null) {
                onConfirm.run();
            }
            this.close();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}
