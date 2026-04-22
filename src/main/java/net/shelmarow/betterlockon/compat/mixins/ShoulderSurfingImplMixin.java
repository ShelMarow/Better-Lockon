package net.shelmarow.betterlockon.compat.mixins;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = ShoulderSurfingImpl.class, remap = false)
public class ShoulderSurfingImplMixin {

    @Inject(
            method = "lookAtCrosshairTargetInternal",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void lookAtCrosshairTargetInternal(CallbackInfo ci){
        if(EpicFightCameraAPI.getInstance().isLockingOnTarget()){
            ci.cancel();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingImpl;shouldEntityFollowCamera(Lnet/minecraft/world/entity/LivingEntity;)Z"
            )
    )
    public boolean shouldEntityFollowCamera(ShoulderSurfingImpl instance, LivingEntity cameraEntity){
        if(EpicFightCameraAPI.getInstance().isLockingOnTarget()){
            return false;
        }
        return instance.shouldEntityFollowCamera(cameraEntity);
    }
}
