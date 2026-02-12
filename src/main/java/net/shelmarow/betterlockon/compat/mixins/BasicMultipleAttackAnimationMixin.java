package net.shelmarow.betterlockon.compat.mixins;

import net.shelmarow.betterlockon.config.LockOnConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import reascer.wom.animation.attacks.BasicMultipleAttackAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = BasicMultipleAttackAnimation.class, remap = false)
public class BasicMultipleAttackAnimationMixin {

    @Redirect(
            method = "getCoordVector",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/client/camera/EpicFightCameraAPI;setLockOn(Z)V"
            )
    )
    private void preventLockDisable(EpicFightCameraAPI instance, boolean eventCanceled) {
        if (!LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get()) {
            EpicFightCameraAPI.getInstance().setLockOn(eventCanceled);
        }
    }
}
