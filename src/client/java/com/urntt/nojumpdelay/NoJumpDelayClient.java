package com.urntt.nojumpdelay;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.nojumpdelay.config.NoJumpDelayConfig;
import com.urntt.nojumpdelay.config.NoJumpDelayConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class NoJumpDelayClient implements ClientModInitializer {
	public static final String MOD_ID = "nojumpdelay";
	public static final String TOGGLE_KEY_NAME = "key.nojumpdelay.toggle";
	public static final String OPEN_SETTINGS_KEY_NAME = "key.nojumpdelay.open_settings";

	/** Translation key of the feature's name, shared by the toggle message and the configuration screen. */
	public static final String FEATURE_NAME_KEY = "options.nojumpdelay.enabled";
	public static final Component FEATURE_NAME = Component.translatable(FEATURE_NAME_KEY);
	/** Action bar message shown when the toggle key is pressed on a server the multiplayer rules rule out. */
	public static final Component BLOCKED_MESSAGE = Component.translatable("message.nojumpdelay.blocked");

	private static NoJumpDelayConfig config;
	private static JumpDelayController controller;

	@Override
	public void onInitializeClient() {
		config = NoJumpDelayConfig.load(NoJumpDelayConfig.defaultPath());
		controller = new JumpDelayController(config);

		KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
		KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(TOGGLE_KEY_NAME, InputConstants.UNKNOWN.getValue(), category));
		KeyMapping openSettingsKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(OPEN_SETTINGS_KEY_NAME, InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				toggle(client);
			}
			while (openSettingsKey.consumeClick()) {
				client.gui.setScreen(new NoJumpDelayConfigScreen(client.gui.screen()));
			}
		});

		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> controller.onJoin(Scene.of(client)));
		// The disconnect event may arrive on the network thread; the controller is only used on the client thread.
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> client.execute(controller::onDisconnect));
	}

	public static NoJumpDelayConfig config() {
		return config;
	}

	public static JumpDelayController controller() {
		return controller;
	}

	private static void toggle(final Minecraft client) {
		JumpDelayController.ToggleResult result = controller.toggle();
		if (client.player == null) {
			return;
		}

		Component message = switch (result) {
			case ENABLED -> CommonComponents.optionStatus(FEATURE_NAME, true);
			case DISABLED -> CommonComponents.optionStatus(FEATURE_NAME, false);
			case BLOCKED -> BLOCKED_MESSAGE;
		};
		client.player.sendOverlayMessage(message);
	}
}
