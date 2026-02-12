package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.ThirdPersonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = ThirdPersonEvents.class,remap = false)
public class ThirdPersonEventsMixin {
    @Inject(
            method = "onCalculateMoveImpulse",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void onMoveImpulse(CallbackInfo ci){
        if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
            ci.cancel();
        }
    }
}
