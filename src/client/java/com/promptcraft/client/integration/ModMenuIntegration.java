package com.promptcraft.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.promptcraft.client.gui.ConfigScreen;

/**
 * ModMenu integration for PromptCraft
 * Provides configuration screen access through ModMenu
 */
public class ModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }
}
