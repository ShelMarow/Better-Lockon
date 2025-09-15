package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.core.CameraAgent;
import com.github.leawind.thirdperson.mixin.CameraInvoker;
import net.shelmarow.betterlockon.mixins.LocalPlayerPatchAccessor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = CameraAgent.class, remap = false)
public class CameraAgentMixin {

    @Unique private float betterLockOn$partialTick = 0;
    @Unique private float betterLockOn$lastLockXRot = 0;
    @Unique private float betterLockOn$lastLockYRot = 0;
    @Unique private boolean betterLockOn$changed = false;
    @Unique private boolean betterLockOn$lockOn = false;

    @Shadow @Final
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
                    target = "Lcom/github/leawind/thirdperson/mixin/CameraInvoker;invokeSetRotation(FF)V"
            )
    )
    private void onUpdateTempCameraRotationPosition(CameraInvoker instance, float v, float t) {
        //由史诗战斗的相机旋转接管
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null) {
            if (localPlayerPatch.getTarget() != null && localPlayerPatch.isTargetLockedOn()) {
                float xRot;
                float yRot;
                if(!betterLockOn$lockOn){
                    LocalPlayerPatchAccessor accessor = (LocalPlayerPatchAccessor)localPlayerPatch;
                    accessor.setLockOnYRot((float) (this.relativeRotation.y + 180.0F));
                    accessor.setLockOnYRotO((float) (this.relativeRotation.y + 180.0F));
                    accessor.setLockOnXRot((float) (-this.relativeRotation.x));
                    accessor.setLockOnXRotO((float) (-this.relativeRotation.x));
                    betterLockOn$lockOn = true;
                }
                xRot = localPlayerPatch.getLerpedLockOnX(this.betterLockOn$partialTick);
                yRot = localPlayerPatch.getLerpedLockOnY(this.betterLockOn$partialTick);
                betterLockOn$lastLockXRot = xRot;
                betterLockOn$lastLockYRot = yRot;
                betterLockOn$changed = true;
                instance.invokeSetRotation(yRot, xRot);
                return;
            }
            if(!localPlayerPatch.isTargetLockedOn()){
                betterLockOn$lockOn = false;
            }
        }

        if(betterLockOn$changed) {
            v = betterLockOn$lastLockYRot;
            relativeRotation.y = betterLockOn$lastLockYRot - 180;
            t = betterLockOn$lastLockXRot;
            relativeRotation.x = -betterLockOn$lastLockXRot;
            betterLockOn$changed = false;
        }
        instance.invokeSetRotation(v, t);
    }
}
