package com.urntt.nojumpdelay.gametest;

import static com.urntt.nojumpdelay.gametest.GameTestSupport.bindKey;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.check;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.checkCooldownRemoved;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.checkVanillaCooldown;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.configure;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.isEnabled;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.placeLowCeiling;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.unbindKey;

import com.urntt.nojumpdelay.NoJumpDelayClient;
import com.urntt.nojumpdelay.Scene;
import com.urntt.nojumpdelay.config.MultiplayerMode;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.minecraft.client.KeyMapping;

/**
 * Checks the multiplayer modes on a local dedicated server, which the client reaches as {@code localhost}.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NoJumpDelayMultiplayerGameTest implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext context) {
		configure(context, config -> {
			config.setEnabled(true);
			config.setMultiplayerDefault(true);
			config.setResetOnWorldExit(false);
			config.setMultiplayerMode(MultiplayerMode.DISABLED);
			config.setServers(List.of());
		});
		KeyMapping toggleKey = bindKey(context, NoJumpDelayClient.TOGGLE_KEY_NAME, "key.keyboard.j");

		try (TestDedicatedServerContext server = context.worldBuilder().createServer();
				TestDedicatedServerConnection connection = server.connect()) {
			connection.waitForChunksRender();
			placeLowCeiling(context, server);
			Scene scene = context.computeOnClient(client -> NoJumpDelayClient.controller().scene());
			GameTestSupport.LOGGER.info("Connected to {}", scene);
			check(scene instanceof Scene.Multiplayer, "a dedicated server should count as multiplayer, got " + scene);

			checkVanillaCooldown(context, "on a server with multiplayer disabled");
			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(isEnabled(context), "a blocked toggle should leave the state unchanged");
			context.takeScreenshot("nojumpdelay-blocked-on-server");

			configure(context, config -> {
				config.setServers(List.of("localhost"));
				config.setMultiplayerMode(MultiplayerMode.WHITELIST);
			});
			checkCooldownRemoved(context, "on a whitelisted server");

			configure(context, config -> config.setMultiplayerMode(MultiplayerMode.BLACKLIST));
			checkVanillaCooldown(context, "on a blacklisted server");
		}

		configure(context, config -> {
			config.setMultiplayerMode(MultiplayerMode.DISABLED);
			config.setServers(List.of());
		});
		unbindKey(context, toggleKey);
	}
}
