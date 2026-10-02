package com.urntt.nojumpdelay.gametest;

import static com.urntt.nojumpdelay.gametest.GameTestSupport.bindKey;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.check;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.checkCooldownRemoved;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.checkVanillaCooldown;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.configure;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.isEnabled;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.loadSavedConfig;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.placeLowCeiling;
import static com.urntt.nojumpdelay.gametest.GameTestSupport.unbindKey;

import com.urntt.nojumpdelay.NoJumpDelayClient;
import com.urntt.nojumpdelay.config.MultiplayerMode;
import com.urntt.nojumpdelay.config.NoJumpDelayConfigScreen;
import com.urntt.nojumpdelay.config.ServerListScreen;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.KeyMapping;

/**
 * Checks the feature, the toggle key, the reset on world exit, and the settings key in singleplayer worlds.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NoJumpDelayClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext context) {
		configure(context, config -> {
			config.setEnabled(true);
			config.setSingleplayerDefault(true);
			config.setResetOnWorldExit(false);
			config.setResetOnGameExit(false);
		});
		KeyMapping toggleKey = bindKey(context, NoJumpDelayClient.TOGGLE_KEY_NAME, "key.keyboard.j");
		KeyMapping openSettingsKey = bindKey(context, NoJumpDelayClient.OPEN_SETTINGS_KEY_NAME, "key.keyboard.k");

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			placeLowCeiling(context, singleplayer.getServer());

			checkCooldownRemoved(context, "in singleplayer with the mod enabled");

			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(!isEnabled(context), "toggle key should disable the feature");
			check(!loadSavedConfig().isEnabled(), "disabled state should be saved to the config file");
			context.takeScreenshot("nojumpdelay-toggled-off");

			checkVanillaCooldown(context, "in singleplayer with the mod disabled");

			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(isEnabled(context), "toggle key should enable the feature again");
			check(loadSavedConfig().isEnabled(), "enabled state should be saved to the config file");

			checkSettingsScreens(context, openSettingsKey);

			// Leave this world disabled to check the reset on world exit below.
			configure(context, config -> config.setResetOnWorldExit(true));
			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(!isEnabled(context), "toggle key should disable the feature before leaving");
		}

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			check(isEnabled(context), "reset on world exit should restore the singleplayer default");

			configure(context, config -> config.setResetOnWorldExit(false));
			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(!isEnabled(context), "toggle key should disable the feature before leaving");
		}

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			check(!isEnabled(context), "without reset on world exit the state should carry over");
		}

		configure(context, config -> config.setEnabled(true));
		unbindKey(context, toggleKey);
		unbindKey(context, openSettingsKey);
	}

	/**
	 * Opens the settings with the key binding, then takes screenshots of both settings screens in English and in
	 * Simplified Chinese.
	 */
	private static void checkSettingsScreens(final ClientGameTestContext context, final KeyMapping openSettingsKey) {
		context.getInput().pressKey(openSettingsKey);
		context.waitForScreen(NoJumpDelayConfigScreen.class);
		context.setScreen(() -> null);

		configure(context, config -> {
			config.setMultiplayerMode(MultiplayerMode.WHITELIST);
			config.setServers(List.of("mc.example.com", "192.168.1.5"));
		});
		// Keep the cursor away from the widgets so no tooltip covers them.
		context.getInput().setCursorPos(0, 0);
		takeSettingsScreenshots(context, "en_us");
		switchLanguage(context, "zh_cn");
		takeSettingsScreenshots(context, "zh_cn");
		switchLanguage(context, "en_us");

		configure(context, config -> {
			config.setMultiplayerMode(MultiplayerMode.DISABLED);
			config.setServers(List.of());
		});
	}

	private static void takeSettingsScreenshots(final ClientGameTestContext context, final String language) {
		context.setScreen(() -> new NoJumpDelayConfigScreen(null));
		context.takeScreenshot("nojumpdelay-config-screen-" + language);
		context.getInput().scroll(-20);
		context.waitTick();
		context.takeScreenshot("nojumpdelay-config-screen-bottom-" + language);

		context.setScreen(() -> new ServerListScreen(new NoJumpDelayConfigScreen(null), NoJumpDelayClient.config()));
		context.takeScreenshot("nojumpdelay-server-list-" + language);
		context.setScreen(() -> null);
	}

	private static void switchLanguage(final ClientGameTestContext context, final String language) {
		CompletableFuture<Void> reload = context.computeOnClient(client -> {
			client.getLanguageManager().setSelected(language);
			client.options.languageCode = language;
			return client.reloadResourcePacks();
		});
		context.waitFor(client -> reload.isDone() && client.gui.overlay() == null);
	}
}
