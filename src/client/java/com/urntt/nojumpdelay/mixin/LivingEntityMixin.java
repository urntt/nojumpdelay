package com.urntt.nojumpdelay.mixin;

import com.urntt.nojumpdelay.NoJumpDelayClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Shadow
	private int noJumpDelay;

	/**
	 * Clears the local player's jump cooldown before {@code aiStep} checks it, so holding the jump key jumps again
	 * as soon as the player lands. Every other entity keeps the vanilla cooldown, and so does the local player
	 * whenever the feature is toggled off or the multiplayer rules rule out the current server.
	 */
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void nojumpdelay$clearJumpDelay(final CallbackInfo ci) {
		if ((Object) this instanceof LocalPlayer && NoJumpDelayClient.controller().isActive()) {
			this.noJumpDelay = 0;
		}
	}
}
