package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.core.CameraAgent;
import com.github.leawind.thirdperson.mixin.CameraInvoker;
import com.github.leawind.thirdperson.util.math.LMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.shelmarow.betterlockon.client.control.BLOCameraSetting;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = CameraAgent.class, remap = false)
public class CameraAgentMixin {

    @Unique private float betterLockOn$partialTick = 0;
    @Unique private float betterLockOn$lastLockXRot = 0;
    @Unique private float betterLockOn$lastLockYRot = 0;
    @Unique private boolean betterLockOn$changed = false;
    @Unique private boolean betterLockOn$lockOn = false;

    @Shadow
    @Final
    private @NotNull Vector2d relativeRotation;

    @Inject(
            method = "updateTempCameraRotationPosition",
            at = @At(
                    "HEAD"
            ),
            remap = false
    )
    private void getP(float partialTick, CallbackInfo ci){
        this.betterLockOn$partialTick = partialTick;
    }

    @Redirect(
            method = "updateTempCameraRotationPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/leawind/thirdperson/util/math/LMath;toVec3(Lorg/joml/Vector3d;)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            remap = false
    )
    private Vec3 setPosition1(Vector3d v){
        Vec3 cameraPos = BLOCameraSetting.getCameraPos(betterLockOn$partialTick);
        return LMath.toVec3(v).add(cameraPos);
    }

    @Redirect(
            method = "updateTempCameraRotationPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/leawind/thirdperson/mixin/CameraInvoker;invokeSetRotation(FF)V"
            )
    )
    private void onUpdateTempCameraRotationPosition(CameraInvoker instance, float y, float x) {
        //由史诗战斗的相机旋转接管
        EpicFightCameraAPI cameraAPI = EpicFightCameraAPI.getInstance();
        if (cameraAPI.isLockingOnTarget()) {
            float xRot;
            float yRot;
            if(!betterLockOn$lockOn){
                cameraAPI.setCameraRotations((float) (-this.relativeRotation.x), (float) (this.relativeRotation.y + 180.0F), true);
                betterLockOn$lockOn = true;
            }
            xRot = Mth.lerp(betterLockOn$partialTick, cameraAPI.getCameraXRotO(), cameraAPI.getCameraXRot());
            yRot = Mth.lerp(betterLockOn$partialTick, cameraAPI.getCameraYRotO(), cameraAPI.getCameraYRot());
            betterLockOn$lastLockXRot = xRot;
            betterLockOn$lastLockYRot = yRot;
            betterLockOn$changed = true;
            instance.invokeSetRotation(yRot, xRot);
            return;
        }
        else{
            betterLockOn$lockOn = false;
        }

        if(betterLockOn$changed) {
            y = betterLockOn$lastLockYRot;
            relativeRotation.y = betterLockOn$lastLockYRot - 180;
            x = betterLockOn$lastLockXRot;
            relativeRotation.x = -betterLockOn$lastLockXRot;
            betterLockOn$changed = false;
        }
        instance.invokeSetRotation(y, x);
    }
}
