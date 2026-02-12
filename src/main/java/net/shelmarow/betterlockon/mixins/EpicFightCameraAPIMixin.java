package net.shelmarow.betterlockon.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.shelmarow.betterlockon.client.control.LockOnControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import java.util.List;

@Mixin(value = EpicFightCameraAPI.class,remap = false)
public abstract class EpicFightCameraAPIMixin {

    @Shadow
    private LivingEntity focusingEntity;
    @Final
    @Shadow private Minecraft minecraft;

    @Shadow
    private boolean couplingYRot;

    @Shadow
    public abstract boolean isLockingOnTarget();

    @Shadow
    public abstract LivingEntity getFocusingEntity();

    @Shadow
    public abstract boolean isTPSMode();

    @Unique
    private boolean betterLockOn$setClosestTarget(){
        LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(Minecraft.getInstance().player,LocalPlayerPatch.class);
        List<LivingEntity> entityList;
        if (playerPatch != null) {
            entityList = LockOnControl.entitiesCanBeSeen(playerPatch.getOriginal(),playerPatch.getOriginal().clientLevel,null,0);
            LivingEntity closest = LockOnControl.selectBestTarget(playerPatch.getOriginal(), entityList,null);
            if(closest != null){
                focusingEntity = closest;
                EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(focusingEntity.getId()));
                return true;
            }
            else{
                focusingEntity = null;
                EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
                return false;
            }
        }
        return false;
    }

    @Unique
    private float better_Lockon$getOffset() {
        float offset = 0;
        //if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceX();

        if (minecraft.options.keySprint.isDown() && !minecraft.options.keyUse.isDown() && minecraft.player != null) {
            Input input = minecraft.player.input;
            float dir = 0;
            boolean forward = input.up && !input.down;
            boolean backward = !input.up && input.down;

            if(input.left && forward) {
                dir = 45;
            }
            else if(input.left && !backward) {
                dir = 90;
            }
            else if(input.left) {
                dir = 135;
            }
            else if(input.right && forward) {
                dir = -45;
            }
            else if(input.right && !backward) {
                dir = -90;
            }
            else if(input.right) {
                dir = -135;
            }
            else if(backward) {
                dir = 180;
            }

            offset += dir;
        }
        return offset;
    }

//    @Inject(
//            method = "setLockOn",
//            at = @At(
//                    value = "HEAD"
//            )
//    )
//    private void onSetLockOn(boolean flag, CallbackInfo ci){
//        if(!isLockingOnTarget() && getFocusingEntity() == null){
//            betterLockOn$setClosestTarget();
//        }
//    }

//    //修改脱落锁定的逻辑；丢失目标后尝试寻找新目标
//    @Inject(
//            method = "clientTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/LivingEntity;isRemoved()Z",
//                    ordinal = 0
//            ),
//            remap = true
//    )
//    private void atTargetLockedOnCheck(CallbackInfo ci) {
//        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
//        double maxRange = LockOnConfig.MAX_LOCK_ON_DISTANCE.get();
//        if (!this.rayTarget.isAlive() || this.rayTarget.isInvisibleTo(playerPatch.getOriginal()) ||
//                playerPatch.getOriginal().distanceToSqr(this.rayTarget) > maxRange*maxRange ||
//                playerPatch.getAngleTo(this.rayTarget) > 100 && !this.targetLockedOn) {
//
//            boolean autoSwitch = LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.get();
//            boolean targetFound = betterLockOn$setClosestTarget();
//
//            //如果没开启自动切换或者找不到目标，解除锁定并清除目标
//            if(!autoSwitch || !targetFound){
//                if(playerPatch.isTargetLockedOn()) {
//                    playerPatch.getOriginal().setXRot(this.lockOnXRot);
//                    playerPatch.getOriginal().setYRot(this.lockOnYRot);
//                    this.targetLockedOn = false;
//                }
//                this.rayTarget = null;
//                EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
//            }
//        }
//    }
//
//    //调整锁定摄像机角度
//    @Redirect(
//            method = "clientTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
//                    ordinal = 0
//            ),
//            remap = true
//    )
//    private float betterlockon$redirectYawClamp(float value, float min, float max) {
//        CameraType cameraType = this.minecraft.options.getCameraType();
//        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
//
//        //计算为目标眼睛高度和玩家脚部高度的差值
//        double ratio = (this.rayTarget.getEyePosition().y - playerPatch.getOriginal().getEyePosition().y + playerPatch.getOriginal().getBbHeight()) / playerPatch.getOriginal().getBbHeight();
//        float heightFactor = (float) Mth.clamp(ratio,1.0F,5F);
//
//        //高度比例决定了视角最终的限制阈值
//        float maxPitch = 90F;
//        float minPitch = (float) Mth.lerp((heightFactor - 1.0F)/ 4F,LockOnConfig.MIN_PITCH_WHEN_LOCK_ON.get(),-45F);
//
//        //俯仰角偏移
//        Double pitchOffset = LockOnConfig.PITCH_OFFSET_WHEN_LOCK_ON.get();
//
//        //第一人称下，不额外做限制
//        if(cameraType == CameraType.FIRST_PERSON) {
//            minPitch = -90F;
//            pitchOffset = 0D;
//        }
//
//        return Mth.clamp((float) (value + pitchOffset), minPitch,maxPitch);
//    }

    /*
     * 添加软锁，开启时允许玩家小幅度自由移动视角
     */
    //左右
//    @Redirect(
//            method = "clientTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lyesman/epicfight/api/utils/math/MathUtils;getYRotOfVector(Lnet/minecraft/world/phys/Vec3;)D"
//            )
//    )
//    private double redirectLockOnYRot(Vec3 vec) {
//        float offset = 0;
//        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceX();
//        return MathUtils.getYRotOfVector(vec) + offset;
//    }
//
    @Redirect(
            method = "postClientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 5
            ),
            remap = true
    )
    private float redirectYRot(float pValue, float pMin, float pMax){
        float offset = better_Lockon$getOffset();
        return pValue - offset;
    }

    @Inject(
            method = "getYRotForHead",
            at = @At("RETURN"),
            cancellable = true
    )
    private  void onGetYRotForHead(Player player, CallbackInfoReturnable<Float> cir){
        if (isLockingOnTarget() && minecraft.options.keySprint.isDown() && !minecraft.options.keyUse.isDown()) {
            cir.setReturnValue(player.getYRot());
        }
    }

//
//    //上下
//    @Redirect(
//            method = "clientTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lyesman/epicfight/api/utils/math/MathUtils;getXRotOfVector(Lnet/minecraft/world/phys/Vec3;)D"
//            )
//    )
//    private double redirectLockOnXRot(Vec3 vec) {
//        float offset = 0;
//        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceY();
//        return MathUtils.getXRotOfVector(vec) + offset;
//    }
//
//    //调整玩家头部模型角度
//    @ModifyArg(
//            method = "clientTick",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/player/LocalPlayer;setXRot(F)V",
//                    ordinal = 0
//            ),
//            remap = true
//    )
//    private float redirectXRot(float par1){
//        float offset = 0;
//        if(LockOnConfig.ENABLE_SOFT_LOCK.get()){
//            offset = LockOnControl.getLastMovedDistanceY();
//        }
//        Vec3 playerEye = ((LocalPlayerPatch) (Object) this).getOriginal().getEyePosition();
//        Vec3 targetEye = this.rayTarget.position().add(0,this.rayTarget.getBbHeight()*2/3,0);
//        Vec3 toTarget = targetEye.subtract(playerEye);
//        double pitchDeg = MathUtils.getXRotOfVector(toTarget);
//        return (float) Mth.clamp(pitchDeg + offset, -45, 45);
//    }
//
//
//    //调整按下锁定键时的索敌逻辑
//    @Inject(
//            method = "setLockOn",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    public void setLockOn(boolean targetLockedOn, CallbackInfo ci) {
//        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
//        LocalPlayer localPlayer = playerPatch.getOriginal();
//        if(!this.targetLockedOn){
//            LockOnControl.setLastMovedDistanceX(0);
//            LockOnControl.setLastMovedDistanceY(0);
//            //如果有锁定目标，检查是否还在视野内
//            //如果被遮挡则取消锁定目标并尝试重新索敌
//            if(this.rayTarget != null){
//                List<LivingEntity> entityList = LockOnControl.entitiesCanBeSeen(localPlayer,localPlayer.clientLevel,null,0);
//                if(!entityList.contains(this.rayTarget)){
//                    rayTarget = null;
//                    EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
//                }
//            }
//
//            if(this.rayTarget == null){
//                betterLockOn$setClosestTarget();
//            }
//
//            if(ModList.get().isLoaded(ShoulderSurfingCommon.MOD_ID)){
//                HandlerShoulderSurfingCompat.handlerCam();
//            }
//        }
//
//        if (this.targetLockedOn && this.rayTarget != null) {
//            localPlayer.setXRot(this.lockOnXRot);
//            localPlayer.setYRot(this.lockOnYRot);
//            EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(rayTarget.getId()));
//        }
//
//        this.targetLockedOn = targetLockedOn;
//        ci.cancel();
//    }
}
