package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.core.CameraAgent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CameraAgent.class, remap = false)
public class CameraAgentMixin {
//
//    @Unique private float betterLockOn$lastLockXRot = 0;
//    @Unique private float betterLockOn$lastLockYRot = 0;
//    @Unique private boolean betterLockOn$changed = false;
//    @Unique private boolean betterLockOn$lockOn = false;
//
//    @Shadow @Final
//    private @NotNull Vector2d relativeRotation;
//
//    @Redirect(
//            method = "updateTempCameraRotationPosition",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lcom/github/leawind/thirdperson/mixin/CameraInvoker;invokeSetRotation(FF)V"
//            )
//    )
//    private void onUpdateTempCameraRotationPosition(CameraInvoker instance, float v, float t) {
//        //由史诗战斗的相机旋转接管
//        EpicFightCameraAPIAccessor accessor = (EpicFightCameraAPIAccessor) (Object) EpicFightCameraAPI.getInstance();
//        if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
//            float xRot;
//            float yRot;
//            if(!betterLockOn$lockOn){
//                accessor.setCameraYRot((float) (this.relativeRotation.y + 180.0F));
//                accessor.setCameraYRotO((float) (this.relativeRotation.y + 180.0F));
//                accessor.setCameraXRot((float) (-this.relativeRotation.x));
//                accessor.setCameraXRotO((float) (-this.relativeRotation.x));
//                betterLockOn$lockOn = true;
//            }
//            xRot = EpicFightCameraAPI.getInstance().getCameraXRotO();
//            yRot = EpicFightCameraAPI.getInstance().getCameraYRotO();
//            betterLockOn$lastLockXRot = xRot;
//            betterLockOn$lastLockYRot = yRot;
//            betterLockOn$changed = true;
//            instance.invokeSetRotation(yRot, xRot);
//            return;
//        }
//        else{
//            betterLockOn$lockOn = false;
//        }
//
//        if(betterLockOn$changed) {
//            v = betterLockOn$lastLockYRot;
//            relativeRotation.y = betterLockOn$lastLockYRot - 180;
//            t = betterLockOn$lastLockXRot;
//            relativeRotation.x = -betterLockOn$lastLockXRot;
//            betterLockOn$changed = false;
//        }
//        instance.invokeSetRotation(v, t);
//    }
}
