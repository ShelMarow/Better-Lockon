package net.shelmarow.betterlockon.mixins;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.shelmarow.betterlockon.client.control.LockOnControl;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSetPlayerTarget;

import java.util.List;

@Mixin(value = LocalPlayerPatch.class,remap = false)
public class LocalPlayerPatchMixin {

    @Shadow private LivingEntity rayTarget;
    @Shadow private float lockOnXRot;
    @Shadow private float lockOnYRot;
    @Shadow private boolean targetLockedOn;
    @Shadow private Minecraft minecraft;

    @Unique
    private boolean betterLockOn$setClosestTarget(){
        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
        List<LivingEntity> entityList = LockOnControl.entitiesCanBeSeen(playerPatch.getOriginal(),playerPatch.getOriginal().clientLevel,null,0);
        LivingEntity closest = LockOnControl.selectBestTarget(playerPatch.getOriginal(), entityList,null);
        if(closest != null){
            rayTarget = closest;
            EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(rayTarget.getId()));
            return true;
        }
        else{
            rayTarget = null;
            EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
            return false;
        }
    }

    //修改脱落锁定的逻辑；丢失目标后尝试寻找新目标
    @Inject(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isRemoved()Z",
                    ordinal = 0
            ),
            remap = true
    )
    private void atTargetLockedOnCheck(CallbackInfo ci) {
        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
        double maxRange = LockOnConfig.MAX_LOCK_ON_DISTANCE.get();
        if (!this.rayTarget.isAlive() || this.rayTarget.isInvisibleTo(playerPatch.getOriginal()) ||
                playerPatch.getOriginal().distanceToSqr(this.rayTarget) > maxRange*maxRange ||
                playerPatch.getAngleTo(this.rayTarget) > 100 && !this.targetLockedOn) {

            boolean autoSwitch = LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.get();
            boolean targetFound = betterLockOn$setClosestTarget();

            //如果没开启自动切换或者找不到目标，解除锁定并清除目标
            if(!autoSwitch || !targetFound){
                if(playerPatch.isTargetLockedOn()) {
                    playerPatch.getOriginal().setXRot(this.lockOnXRot);
                    playerPatch.getOriginal().setYRot(this.lockOnYRot);
                    this.targetLockedOn = false;
                }
                this.rayTarget = null;
                EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
            }
        }
    }

    //取消原有if逻辑
    //为什么没有方法取消整个if语句，KUSO
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isRemoved()Z"
            )
    )
    private boolean redirectIsRemoved(LivingEntity instance) {
        return false;
    }
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"
            ),
            remap = true
    )
    private boolean redirectIsInvisibleTo(LivingEntity instance, Player player) {
        return false;
    }
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"
            ),
            remap = true
    )
    private double redirectDistanceToSqr(LocalPlayer instance, Entity entity) {
        return 0.0D;
    }
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;getAngleTo(Lnet/minecraft/world/entity/Entity;)D"
            )
    )
    private double redirectGetAngleTo(LocalPlayerPatch instance, Entity entity) {
        return 0.0D;
    }

    //调整锁定摄像机角度
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 0
            ),
            remap = true
    )
    private float betterlockon$redirectYawClamp(float value, float min, float max) {
        CameraType cameraType = this.minecraft.options.getCameraType();
        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);

        //计算为目标眼睛高度和玩家脚部高度的差值
        double ratio = (this.rayTarget.getEyePosition().y - playerPatch.getOriginal().getEyePosition().y + playerPatch.getOriginal().getBbHeight()) / playerPatch.getOriginal().getBbHeight();
        float heightFactor = (float) Mth.clamp(ratio,1.0F,5F);

        //高度比例决定了视角最终的限制阈值
        float maxPitch = 90F;
        float minPitch = (float) Mth.lerp((heightFactor - 1.0F)/ 4F,LockOnConfig.MIN_PITCH_WHEN_LOCK_ON.get(),-45F);

        //俯仰角偏移
        Double pitchOffset = LockOnConfig.PITCH_OFFSET_WHEN_LOCK_ON.get();

        //第一人称下，不额外做限制
        if(cameraType == CameraType.FIRST_PERSON) {
            minPitch = -90F;
            pitchOffset = 0D;
        }

        return Mth.clamp((float) (value + pitchOffset), minPitch,maxPitch);
    }

    /*
     * 添加软锁，开启时允许玩家小幅度自由移动视角
     */

    //左右
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/utils/math/MathUtils;getYRotOfVector(Lnet/minecraft/world/phys/Vec3;)D"
            )
    )
    private double redirectLockOnYRot(Vec3 vec) {
        float offset = 0;
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceX();
        return MathUtils.getYRotOfVector(vec) + offset;
    }

    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V",
                    ordinal = 0
            ),
            remap = true
    )
    private void redirectYRot(LocalPlayer instance, float v){
        float offset = 0;
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceX();
        instance.setYRot(v - offset);
    }

    //上下
    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/utils/math/MathUtils;getXRotOfVector(Lnet/minecraft/world/phys/Vec3;)D"
            )
    )
    private double redirectLockOnXRot(Vec3 vec) {
        float offset = 0;
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceY();
        return MathUtils.getXRotOfVector(vec) + offset;
    }

    //调整玩家头部模型角度
    @ModifyArg(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setXRot(F)V",
                    ordinal = 0
            ),
            remap = true
    )
    private float redirectXRot(float par1){
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()){
            float offset = 0;
            if(LockOnConfig.ENABLE_SOFT_LOCK.get()) offset = LockOnControl.getLastMovedDistanceY();
            return Mth.clamp(par1 + offset, -60, 60);
        }
        else {
            Vec3 playerEye = ((LocalPlayerPatch) (Object) this).getOriginal().getEyePosition();
            Vec3 targetEye = this.rayTarget.getEyePosition();
            Vec3 toTarget = targetEye.subtract(playerEye);
            double pitchDeg = MathUtils.getXRotOfVector(toTarget);
            return (float) Mth.clamp(pitchDeg, -60, 60);
        }
    }


    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 1
            ),
            remap = true
    )
    private float redirectY(float pValue, float pMin, float pMax){
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) {
            pValue /= 0.4F;
            return Mth.clamp(pValue * 0.2F, -4F, 4F);
        }
        else return Mth.clamp(pValue, pMin, pMax);
    }

    @Redirect(
            method = "clientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;clamp(FFF)F",
                    ordinal = 2
            ),
            remap = true
    )
    private float redirectX(float pValue, float pMin, float pMax){
        if(LockOnConfig.ENABLE_SOFT_LOCK.get()) {
            pValue /= 0.4F;
            return Mth.clamp(pValue * 0.2F, -4F, 4F);
        }
        else return Mth.clamp(pValue, pMin, pMax);
    }


    //调整按下锁定键时的索敌逻辑
    @Inject(
            method = "setLockOn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void setLockOn(boolean targetLockedOn, CallbackInfo ci) {
        LocalPlayerPatch playerPatch = ((LocalPlayerPatch) (Object) this);
        LocalPlayer localPlayer = playerPatch.getOriginal();
        LockOnControl.setLastMovedDistanceX(0);
        LockOnControl.setLastMovedDistanceY(0);
        if(!this.targetLockedOn){
            //如果有锁定目标，检查是否还在视野内
            //如果被遮挡则取消锁定目标并尝试重新索敌
            if(this.rayTarget != null){
                List<LivingEntity> entityList = LockOnControl.entitiesCanBeSeen(localPlayer,localPlayer.clientLevel,null,0);
                if(!entityList.contains(this.rayTarget)){
                    rayTarget = null;
                    EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
                }
            }

            if(this.rayTarget == null){
                betterLockOn$setClosestTarget();
            }
        }

        if (this.targetLockedOn && this.rayTarget != null) {
            localPlayer.setXRot(this.lockOnXRot);
            localPlayer.setYRot(this.lockOnYRot);
            EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(rayTarget.getId()));
        }

        this.targetLockedOn = targetLockedOn;
        ci.cancel();
    }
}
