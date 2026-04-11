package com.urntt;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.mixin.client.LivingEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class NoJumpDelayClient implements ClientModInitializer {
	private static final KeyMapping.Category AIR_JUMP_CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath("nojumpdelay", "air_jump"));

	private static final KeyMapping AIR_JUMP_MODIFIER = KeyMappingHelper.registerKeyMapping(
			new KeyMapping(
					"key.nojumpdelay.air_jump_modifier",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_R,
					AIR_JUMP_CATEGORY
			)
	);

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
	}

	private void onEndClientTick(final Minecraft client) {
		if (client.player == null) {
			return;
		}

		((LivingEntityAccessor) client.player).setNoJumpDelay(0);

		if (client.screen != null) {
			return;
		}

		while (client.options.keyJump.consumeClick()) {
			if (!client.player.onGround() && AIR_JUMP_MODIFIER.isDown()) {
				client.player.jumpFromGround();
			}
		}
	}
}