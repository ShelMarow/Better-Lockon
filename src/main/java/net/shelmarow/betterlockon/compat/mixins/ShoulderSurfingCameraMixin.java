package net.shelmarow.betterlockon.compat.mixins;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import com.github.exopandora.shouldersurfing.math.Vec2f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.shelmarow.betterlockon.client.control.BLOCameraSetting;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class ShoulderSurfingCameraMixin {

    @Shadow private Vec3 targetOffset;
    @Shadow private float xRot;

    @Unique private float blo$lastXRot;
    @Unique private boolean blo$Lockon = false;

    @Inject(
            method = "calcOffset",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingCamera;calcCameraDrag(Lnet/minecraft/client/Camera;Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private void onCalcOffset(Camera camera, BlockGetter level, float partialTick, Entity cameraEntity, CallbackInfoReturnable<Vec3> cir){
        if(LockOnConfig.ENABLE_DYNAMIC_CAMERA.get()){
            Vec3 pos = BLOCameraSetting.getCameraPos(partialTick);
            float front = (float) new Vec3(pos.x, 0, pos.z).length();
            this.targetOffset = this.targetOffset.add(0, pos.y, front);
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "HEAD")
    )
    private void onTick(CallbackInfo ci){
        if(EpicFightCameraAPI.getInstance().isLockingOnTarget()){
            xRot = Mth.clamp(
                    EpicFightCameraAPI.getInstance().getCameraXRot() + LockOnConfig.PITCH_OFFSET.get().floatValue(),
                    LockOnConfig.MAX_PITCH.get().floatValue(),
                    LockOnConfig.MIN_PITCH.get().floatValue()
            );
        }
    }

    @Inject(
            method = "calcRotations",
            at = @At(
                    value = "RETURN"
            )
    )
    private void onCalcRotation(Entity cameraEntity, float partialTick, CallbackInfoReturnable<Vec2f> cir){
        EpicFightCameraAPI cameraAPI = EpicFightCameraAPI.getInstance();
        ShoulderSurfingCamera surfingCamera = ShoulderSurfingImpl.getInstance().getCamera();
        if(cameraAPI.isLockingOnTarget()){
            blo$Lockon = true;
            if(cameraAPI.getFocusingEntity() != null){
                blo$lastXRot = cir.getReturnValue().x();
            }
        }
        else if(blo$Lockon){
            blo$Lockon = false;
            surfingCamera.setXRot(blo$lastXRot);
            Minecraft.getInstance().player.setXRot(blo$lastXRot);
        }
    }
}

