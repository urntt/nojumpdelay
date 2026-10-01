package com.urntt.nojumpdelay.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.nojumpdelay.NoJumpDelayClient;
import com.urntt.nojumpdelay.config.NoJumpDelayConfig;
import com.urntt.nojumpdelay.config.NoJumpDelayConfigScreen;
import java.util.Objects;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public final class NoJumpDelayClientGameTest implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("nojumpdelay-gametest");

	/** Number of ticks the jump key is held while counting jumps. */
	private static final int OBSERVED_TICKS = 100;
	/** Vanilla cooldown between two jumps, in ticks. */
	private static final int VANILLA_JUMP_DELAY = 10;
	/** Upper bound on the jumps vanilla allows within {@link #OBSERVED_TICKS}. */
	private static final int MAX_VANILLA_JUMPS = OBSERVED_TICKS / VANILLA_JUMP_DELAY + 1;

	@Override
	public void runTest(final ClientGameTestContext context) {
		KeyMapping toggleKey = Objects.requireNonNull(KeyMapping.get(NoJumpDelayClient.TOGGLE_KEY_NAME), "toggle key mapping");

		// The run directory is deleted before each run, so this is the state of a fresh installation.
		check(isEnabled(context), "the feature should be enabled by default");
		check(loadSavedConfig().isEnabled(), "the config file should be created with the feature enabled");

		context.runOnClient(client -> {
			toggleKey.setKey(InputConstants.getKey("key.keyboard.j"));
			KeyMapping.resetMapping();
		});

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getConnection().waitForChunksRender();
			placeLowCeiling(context, singleplayer);

			int enabledJumps = countJumps(context);
			LOGGER.info("Jumps in {} ticks with the mod enabled: {}", OBSERVED_TICKS, enabledJumps);
			check(enabledJumps > MAX_VANILLA_JUMPS,
					"expected more than " + MAX_VANILLA_JUMPS + " jumps with the mod enabled, got " + enabledJumps);

			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(!isEnabled(context), "toggle key should disable the feature");
			check(!loadSavedConfig().isEnabled(), "disabled state should be saved to the config file");
			context.takeScreenshot("nojumpdelay-toggled-off");

			int disabledJumps = countJumps(context);
			LOGGER.info("Jumps in {} ticks with the mod disabled: {}", OBSERVED_TICKS, disabledJumps);
			check(disabledJumps <= MAX_VANILLA_JUMPS,
					"expected at most " + MAX_VANILLA_JUMPS + " jumps with the mod disabled, got " + disabledJumps);

			context.getInput().pressKey(toggleKey);
			context.waitTick();
			check(isEnabled(context), "toggle key should enable the feature again");
			check(loadSavedConfig().isEnabled(), "enabled state should be saved to the config file");
		}

		context.setScreen(() -> new NoJumpDelayConfigScreen(null));
		context.takeScreenshot("nojumpdelay-config-screen");
		context.setScreen(() -> null);

		context.runOnClient(client -> {
			toggleKey.setKey(toggleKey.getDefaultKey());
			KeyMapping.resetMapping();
		});
	}

	/**
	 * Surrounds the space above the player's head with blocks 0.2 blocks above it, so each jump ends after a few
	 * ticks, well within the vanilla cooldown.
	 */
	private static void placeLowCeiling(final ClientGameTestContext context, final TestSingleplayerContext singleplayer) {
		singleplayer.getServer().runCommand("gamemode survival @a");
		context.waitFor(client -> client.player.onGround());

		BlockPos feet = context.computeOnClient(client -> client.player.blockPosition());
		BlockPos ceiling = feet.above(2);
		singleplayer.getServer().runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(
				ceiling.getX() - 1, ceiling.getY(), ceiling.getZ() - 1,
				ceiling.getX() + 1, ceiling.getY(), ceiling.getZ() + 1));
		context.waitFor(client -> !client.level.getBlockState(ceiling).isAir());
	}

	/**
	 * Holds the jump key for {@link #OBSERVED_TICKS} ticks and counts how often the player leaves the ground.
	 */
	private static int countJumps(final ClientGameTestContext context) {
		context.getInput().holdKey(options -> options.keyJump);

		int jumps = 0;
		boolean wasOnGround = context.computeOnClient(client -> client.player.onGround());
		for (int tick = 0; tick < OBSERVED_TICKS; tick++) {
			context.waitTick();
			boolean onGround = context.computeOnClient(client -> client.player.onGround());
			if (wasOnGround && !onGround) {
				jumps++;
			}
			wasOnGround = onGround;
		}

		context.getInput().releaseKey(options -> options.keyJump);
		context.waitFor(client -> client.player.onGround());
		return jumps;
	}

	private static boolean isEnabled(final ClientGameTestContext context) {
		return context.computeOnClient(client -> NoJumpDelayClient.config().isEnabled());
	}

	private static NoJumpDelayConfig loadSavedConfig() {
		return NoJumpDelayConfig.load(NoJumpDelayConfig.defaultPath());
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
