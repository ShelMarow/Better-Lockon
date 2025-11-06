package net.shelmarow.betterlockon.compat.mixins;

import net.shelmarow.betterlockon.config.LockOnConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import reascer.wom.animation.attacks.BasicMultipleAttackAnimation;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = BasicMultipleAttackAnimation.class, remap = false)
public class BasicMultipleAttackAnimationMixin {

    @Redirect(method = "getCoordVector", at = @At(value = "INVOKE",
            target = "Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;setLockOn(Z)V"))
    private void preventLockDisable(LocalPlayerPatch instance, boolean lockOn) {
        if (!LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get()) {
            instance.setLockOn(lockOn);
        }
    }
}
