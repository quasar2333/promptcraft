package com.promptcraft.client.gui;

import com.promptcraft.config.ConfigManager;
import com.promptcraft.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Blacklist configuration screen
 */
@OnlyIn(Dist.CLIENT)
public class BlacklistScreen extends Screen {
    private final Screen parent;

    private EditBox newKeywordField;
    private List<String> keywords;
    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 8;

    public BlacklistScreen(Screen parent) {
        super(Component.translatable("gui.promptcraft.blacklist.title"));
        this.parent = parent;
        this.keywords = new ArrayList<>(ConfigManager.getConfig().blacklistedKeywords);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;

        // New keyword input field
        this.newKeywordField = new EditBox(this.font, centerX - 100, 50, 150, 18,
                Component.translatable("gui.promptcraft.blacklist.new"));
        this.newKeywordField.setMaxLength(50);
        this.newKeywordField.setHint(Component.translatable("gui.promptcraft.blacklist.placeholder"));
        this.addRenderableWidget(this.newKeywordField);

        // Add button
        this.addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> addKeyword())
                .bounds(centerX + 55, 50, 20, 18)
                .build());

        // Scroll buttons
        this.addRenderableWidget(Button.builder(
                Component.literal("▲"),
                button -> scrollUp())
                .bounds(centerX + 100, 80, 20, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                Component.literal("▼"),
                button -> scrollDown())
                .bounds(centerX + 100, this.height - 80, 20, 20)
                .build());

        // Save button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.config.save"),
                button -> saveAndClose())
                .bounds(centerX - 105, this.height - 40, 100, 20)
                .build());

        // Cancel button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.promptcraft.config.cancel"),
                button -> cancel())
                .bounds(centerX + 5, this.height - 40, 100, 20)
                .build());

        rebuildKeywordButtons();
    }

    private void rebuildKeywordButtons() {
        // Remove old keyword buttons (keep other widgets)
        this.children().removeIf(child -> child instanceof Button btn &&
                btn.getMessage().getString().startsWith("✕ "));

        int centerX = this.width / 2;
        int startY = 80;

        // Add keyword buttons for visible items
        for (int i = 0; i < ITEMS_PER_PAGE && i + scrollOffset < keywords.size(); i++) {
            int index = i + scrollOffset;
            String keyword = keywords.get(index);

            Button removeButton = Button.builder(
                    Component.literal("✕ " + keyword).withStyle(ChatFormatting.RED),
                    button -> removeKeyword(keyword))
                    .bounds(centerX - 100, startY + i * 22, 180, 18)
                    .build();

            this.addRenderableWidget(removeButton);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        // Title
        Component title = this.title;
        int titleWidth = this.font.width(title);
        guiGraphics.drawString(this.font, title, centerX - titleWidth / 2, 20, 0xFFFFFF, true);

        // Count
        String countText = String.format("(%d keywords)", keywords.size());
        guiGraphics.drawString(this.font, countText, centerX - this.font.width(countText) / 2, 35, 0xAAAAAA, false);

        // Empty list message
        if (keywords.isEmpty()) {
            Component emptyMsg = Component.translatable("gui.promptcraft.blacklist.empty")
                    .withStyle(ChatFormatting.GRAY);
            int emptyWidth = this.font.width(emptyMsg);
            guiGraphics.drawString(this.font, emptyMsg, centerX - emptyWidth / 2, 100, 0xAAAAAA, false);
        }
    }

    private void addKeyword() {
        String keyword = newKeywordField.getValue().trim();
        if (!keyword.isEmpty() && !keywords.contains(keyword)) {
            keywords.add(keyword);
            newKeywordField.setValue("");
            rebuildKeywordButtons();
        }
    }

    private void removeKeyword(String keyword) {
        keywords.remove(keyword);
        if (scrollOffset > 0 && scrollOffset >= keywords.size()) {
            scrollOffset = Math.max(0, keywords.size() - ITEMS_PER_PAGE);
        }
        rebuildKeywordButtons();
    }

    private void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
            rebuildKeywordButtons();
        }
    }

    private void scrollDown() {
        if (scrollOffset + ITEMS_PER_PAGE < keywords.size()) {
            scrollOffset++;
            rebuildKeywordButtons();
        }
    }

    private void saveAndClose() {
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        config.blacklistedKeywords.clear();
        config.blacklistedKeywords.addAll(keywords);
        ConfigManager.saveConfig();

        // Send update to server
        NetworkHandler.sendBlacklistUpdate(keywords);

        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void cancel() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
