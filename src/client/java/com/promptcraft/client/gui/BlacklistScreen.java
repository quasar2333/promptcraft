package com.promptcraft.client.gui;

import com.promptcraft.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Blacklist configuration screen for managing blocked keywords
 */
public class BlacklistScreen extends Screen {
    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 320;
    private static final int ITEMS_PER_PAGE = 12;

    private final Screen parent;
    private List<String> blacklistedKeywords;
    private TextFieldWidget addKeywordField;
    private int scrollOffset = 0;
    private int selectedIndex = -1;

    public BlacklistScreen(Screen parent) {
        super(Text.translatable("gui.promptcraft.blacklist.title"));
        this.parent = parent;
        this.blacklistedKeywords = new ArrayList<>(ConfigManager.getConfig().blacklistedKeywords);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        // Add keyword field
        this.addKeywordField = new TextFieldWidget(
                this.textRenderer,
                centerX - 100,
                startY + 32,
                160,
                18,
                Text.translatable("gui.promptcraft.blacklist.placeholder"));
        this.addKeywordField.setMaxLength(50);
        try {
            this.addKeywordField.setDrawsBackground(false);
        } catch (Throwable ignored) {
        }
        // Placeholder API may not exist on this MC version; fallback by drawing hint
        // text
        this.addDrawableChild(this.addKeywordField);
        this.setInitialFocus(this.addKeywordField);

        // Add button
        ButtonWidget addButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.blacklist.add"),
                button -> addKeyword()).dimensions(centerX + 70, startY + 32, 50, 18).build();
        this.addDrawableChild(addButton);

        // Remove button
        ButtonWidget removeButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.blacklist.remove"),
                button -> removeSelectedKeyword()).dimensions(centerX - 80, startY + 260, 60, 18).build();
        this.addDrawableChild(removeButton);

        // Scroll up button
        ButtonWidget scrollUpButton = ButtonWidget.builder(
                Text.literal("↑"),
                button -> scrollUp()).dimensions(centerX + 130, startY + 60, 18, 18).build();
        this.addDrawableChild(scrollUpButton);

        // Scroll down button
        ButtonWidget scrollDownButton = ButtonWidget.builder(
                Text.literal("↓"),
                button -> scrollDown()).dimensions(centerX + 130, startY + 220, 18, 18).build();
        this.addDrawableChild(scrollDownButton);

        // Save button
        ButtonWidget saveButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.config.save"),
                button -> saveBlacklist()).dimensions(centerX - 50, startY + 290, 60, 18).build();
        this.addDrawableChild(saveButton);

        // Cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
                Text.translatable("gui.promptcraft.config.cancel"),
                button -> this.close()).dimensions(centerX + 20, startY + 290, 60, 18).build();
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Single widget layer - render widgets first
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;

        super.render(context, mouseX, mouseY, delta);

        // Draw all text content AFTER widgets to ensure same layer
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, startY - 10, 0xFFFFFFFF);

        // Draw blacklisted keywords (same layer as widgets)
        renderBlacklistItems(context, centerX - 120, startY + 60, mouseX, mouseY);

        // Draw scroll indicator
        if (blacklistedKeywords.size() > ITEMS_PER_PAGE) {
            int totalPages = (blacklistedKeywords.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
            int currentPage = scrollOffset / ITEMS_PER_PAGE + 1;
            String pageInfo = currentPage + "/" + totalPages;
            Text pageText = Text.literal(pageInfo);
            int pageWidth = this.textRenderer.getWidth(pageText);
            context.drawText(this.textRenderer, pageText, centerX + 120 - pageWidth / 2, startY + 305, 0xFFFFFFFF,
                    true);
        }
    }

    private void renderBlacklistItems(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int itemHeight = 16;
        int maxItems = Math.min(ITEMS_PER_PAGE, blacklistedKeywords.size() - scrollOffset);

        for (int i = 0; i < maxItems; i++) {
            int index = scrollOffset + i;
            if (index >= blacklistedKeywords.size())
                break;

            String keyword = blacklistedKeywords.get(index);
            int itemY = y + i * itemHeight;

            // Check if mouse is over this item
            boolean isHovered = mouseX >= x && mouseX <= x + 240 &&
                    mouseY >= itemY && mouseY <= itemY + itemHeight;

            // Highlight selected or hovered item
            if (index == selectedIndex) {
                context.fill(x - 2, itemY - 1, x + 242, itemY + itemHeight - 1, 0xFF404040);
            } else if (isHovered) {
                context.fill(x - 2, itemY - 1, x + 242, itemY + itemHeight - 1, 0xFF202020);
            }

            // Draw keyword
            Text keywordText = Text.literal(keyword);
            context.drawText(this.textRenderer, keywordText, x, itemY + 3, 0xFFFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle blacklist item selection
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int startY = centerY - WINDOW_HEIGHT / 2;
        int listX = centerX - 170;
        int listY = startY + 90;

        if (mouseX >= listX && mouseX <= listX + 300 &&
                mouseY >= listY && mouseY <= listY + ITEMS_PER_PAGE * 18) {

            int clickedIndex = (int) ((mouseY - listY) / 18) + scrollOffset;
            if (clickedIndex < blacklistedKeywords.size()) {
                selectedIndex = clickedIndex;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void addKeyword() {
        String keyword = this.addKeywordField.getText().trim();
        if (!keyword.isEmpty() && !blacklistedKeywords.contains(keyword)) {
            blacklistedKeywords.add(keyword);
            this.addKeywordField.setText("");
        }
    }

    private void removeSelectedKeyword() {
        if (selectedIndex >= 0 && selectedIndex < blacklistedKeywords.size()) {
            blacklistedKeywords.remove(selectedIndex);
            selectedIndex = -1;

            // Adjust scroll offset if needed
            if (scrollOffset > 0 && scrollOffset >= blacklistedKeywords.size()) {
                scrollOffset = Math.max(0, blacklistedKeywords.size() - ITEMS_PER_PAGE);
            }
        }
    }

    private void scrollUp() {
        scrollOffset = Math.max(0, scrollOffset - 1);
    }

    private void scrollDown() {
        int maxScroll = Math.max(0, blacklistedKeywords.size() - ITEMS_PER_PAGE);
        scrollOffset = Math.min(maxScroll, scrollOffset + 1);
    }

    private void saveBlacklist() {
        ConfigManager.PromptCraftConfig config = ConfigManager.getConfig();
        config.blacklistedKeywords.clear();
        config.blacklistedKeywords.addAll(blacklistedKeywords);
        ConfigManager.saveConfig();
        this.close();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
