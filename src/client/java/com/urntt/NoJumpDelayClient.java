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
	private boolean wasOnGround;
	private boolean airJumpReleased = true;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
	}

	private void onEndClientTick(final Minecraft client) {
		if (client.player == null) {
			wasOnGround = false;
			airJumpReleased = true;
			return;
		}

		((LivingEntityAccessor) client.player).setNoJumpDelay(0);

		if (client.screen != null) {
			wasOnGround = false;
			airJumpReleased = true;
			return;
		}

		final boolean onGround = client.player.onGround();
		final boolean jumpKeyDown = client.options.keyJump.isDown();

		if (!jumpKeyDown) {
			airJumpReleased = true;
		}

		while (client.options.keyJump.consumeClick()) {
			if (AIR_JUMP_MODIFIER.isDown()
					&& !onGround
					&& !wasOnGround
					&& airJumpReleased) {
				client.player.jumpFromGround();
				airJumpReleased = false;
			}
		}

		wasOnGround = onGround;
	}
}