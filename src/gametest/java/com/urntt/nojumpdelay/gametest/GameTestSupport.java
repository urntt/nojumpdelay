package com.urntt.nojumpdelay.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import com.urntt.nojumpdelay.NoJumpDelayClient;
import com.urntt.nojumpdelay.config.NoJumpDelayConfig;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared helpers for the client game tests.
 */
@SuppressWarnings("UnstableApiUsage")
final class GameTestSupport {
	static final Logger LOGGER = LoggerFactory.getLogger("nojumpdelay-gametest");

	/** Number of ticks the jump key is held while counting jumps. */
	static final int OBSERVED_TICKS = 100;
	/** Vanilla cooldown between two jumps, in ticks. */
	static final int VANILLA_JUMP_DELAY = 10;
	/** Upper bound on the jumps vanilla allows within {@link #OBSERVED_TICKS}. */
	static final int MAX_VANILLA_JUMPS = OBSERVED_TICKS / VANILLA_JUMP_DELAY + 1;

	private GameTestSupport() {
	}

	/**
	 * Surrounds the space above the player's head with blocks 0.2 blocks above it, so each jump ends after a few
	 * ticks, well within the vanilla cooldown.
	 */
	static void placeLowCeiling(final ClientGameTestContext context, final TestServerContext server) {
		server.runCommand("gamemode survival @a");
		context.waitFor(client -> client.player != null && client.player.onGround());

		BlockPos feet = context.computeOnClient(client -> client.player.blockPosition());
		BlockPos ceiling = feet.above(2);
		server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(
				ceiling.getX() - 1, ceiling.getY(), ceiling.getZ() - 1,
				ceiling.getX() + 1, ceiling.getY(), ceiling.getZ() + 1));
		context.waitFor(client -> !client.level.getBlockState(ceiling).isAir());
	}

	/**
	 * Holds the jump key for {@link #OBSERVED_TICKS} ticks and counts how often the player leaves the ground.
	 */
	static int countJumps(final ClientGameTestContext context, final String situation) {
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
		LOGGER.info("Jumps in {} ticks {}: {}", OBSERVED_TICKS, situation, jumps);
		return jumps;
	}

	static void checkCooldownRemoved(final ClientGameTestContext context, final String situation) {
		int jumps = countJumps(context, situation);
		check(jumps > MAX_VANILLA_JUMPS,
				"expected more than " + MAX_VANILLA_JUMPS + " jumps " + situation + ", got " + jumps);
	}

	static void checkVanillaCooldown(final ClientGameTestContext context, final String situation) {
		int jumps = countJumps(context, situation);
		check(jumps <= MAX_VANILLA_JUMPS,
				"expected at most " + MAX_VANILLA_JUMPS + " jumps " + situation + ", got " + jumps);
	}

	/**
	 * Returns the mod's key mapping registered under {@code name}, bound to {@code key} for the test.
	 */
	static KeyMapping bindKey(final ClientGameTestContext context, final String name, final String key) {
		KeyMapping mapping = Objects.requireNonNull(KeyMapping.get(name), name);
		context.runOnClient(client -> {
			mapping.setKey(InputConstants.getKey(key));
			KeyMapping.resetMapping();
		});
		return mapping;
	}

	static void unbindKey(final ClientGameTestContext context, final KeyMapping mapping) {
		context.runOnClient(client -> {
			mapping.setKey(mapping.getDefaultKey());
			KeyMapping.resetMapping();
		});
	}

	/**
	 * Changes the mod's configuration on the client thread.
	 */
	static void configure(final ClientGameTestContext context, final Consumer<NoJumpDelayConfig> change) {
		context.runOnClient(client -> change.accept(NoJumpDelayClient.config()));
	}

	static boolean isEnabled(final ClientGameTestContext context) {
		return context.computeOnClient(client -> NoJumpDelayClient.config().isEnabled());
	}

	static NoJumpDelayConfig loadSavedConfig() {
		return NoJumpDelayConfig.load(NoJumpDelayConfig.defaultPath());
	}

	static void check(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
