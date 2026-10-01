package com.urntt.nojumpdelay;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.nojumpdelay.config.NoJumpDelayConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class NoJumpDelayClient implements ClientModInitializer {
	public static final String MOD_ID = "nojumpdelay";
	public static final String TOGGLE_KEY_NAME = "key.nojumpdelay.toggle";

	/** Label of the feature, shared by the toggle message and the configuration screen. */
	public static final Component FEATURE_NAME = Component.translatable("options.nojumpdelay.enabled");

	private static NoJumpDelayConfig config;

	@Override
	public void onInitializeClient() {
		config = NoJumpDelayConfig.load(NoJumpDelayConfig.defaultPath());

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
		KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(TOGGLE_KEY_NAME, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				toggle(client);
			}
		});
	}

	public static NoJumpDelayConfig config() {
		return config;
	}

	private static void toggle(final Minecraft client) {
		boolean enabled = !config.isEnabled();
		config.setEnabled(enabled);

		if (client.player != null) {
			client.player.sendOverlayMessage(CommonComponents.optionStatus(FEATURE_NAME, enabled));
		}
	}
}
