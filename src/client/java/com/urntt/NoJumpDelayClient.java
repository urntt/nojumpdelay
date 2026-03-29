package com.urntt;

import com.urntt.mixin.client.LivingEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class NoJumpDelayClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
	}

	private void onEndClientTick(final Minecraft client) {
		if (client.player == null) {
			return;
		}

		((LivingEntityAccessor) client.player).setNoJumpDelay(0);
	}
}