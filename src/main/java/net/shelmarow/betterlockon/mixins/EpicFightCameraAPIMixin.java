package net.shelmarow.betterlockon.mixins;

import com.github.exopandora.shouldersurfing.ShoulderSurfingCommon;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.ModList;
import net.shelmarow.betterlockon.client.control.BLOCameraSetting;
import net.shelmarow.betterlockon.compat.HandlerShoulderSurfingCompat;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.CoupleTPSCamera;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = EpicFightCameraAPI.class, remap = false)
public abstract class EpicFightCameraAPIMixin {

    @Unique private static final float MAX_ZOOM_TICK = 8;
    @Final @Shadow private Minecraft minecraft;
    @Shadow private float cameraXRot;
    @Shadow @Nullable private HitResult crosshairHitResult;
    @Shadow private LivingEntity focusingEntity;
    @Shadow private boolean lockingOnTarget;
    @Shadow private float cameraYRot;
    @Shadow private int fpvLerpTick;
    @Shadow private float fpvXRot;
    @Shadow private float fpvYRot;
    @Shadow private int zoomTick;
    @Shadow private boolean zoomingIn;
    @Shadow private float cameraXRotO;
    @Shadow private float cameraYRotO;
    @Shadow private int quickShiftDelay;
    @Shadow private double accumulatedX;

    @Shadow public abstract boolean isLockingOnTarget();
    @Shadow public abstract boolean isTPSMode();
    @Shadow protected abstract boolean predicateFocusableEntity(Entity entity);
    @Shadow public abstract void zoomIn();
    @Shadow public abstract void zoomOut(int zoomOutTicks);
    @Shadow public abstract void setLockOn(boolean b);
    @Shadow protected abstract void sendTargeting(LivingEntity focusingEntity);
    @Shadow public abstract boolean setNextLockOnTarget(int i, boolean b, boolean b1);
    @Shadow public abstract boolean isFirstPerson();
    @Shadow protected abstract CoupleTPSCamera predicateCouplingPlayer();
    @Shadow public abstract void setCameraRotations(float v, float v1, boolean b);
    @Shadow public abstract boolean isLerpingFpv();
    @Shadow public abstract void fireCameraBuildPost(Camera camera, float partialTick);
    @Shadow protected abstract Matrix4f getCompactProjectionMatrix();


    //瞄准时间计算
    @Unique public boolean blo$isAiming = false;
    @Unique public int blo$maxAimingTick = 8;
    @Unique public int blo$aimingTick;

    //锁定丢失延迟
    @Unique public int blo$maxUnlockDelayTick = 60;
    @Unique public int blo$unlockDelayTick;

    /*
     * 锁定跑步朝向计算
     **/
    @Unique
    private float blo$getOffset() {
        float offset = 0;
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

    @Unique
    private Vec3 blo$getCameraOffset(float partialTick) {
        return BLOCameraSetting.getCameraPos(partialTick);
    }

    @Inject(
            method = "getYRotForHead",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onGetYRotForHead(Player player, CallbackInfoReturnable<Float> cir){
        if (isLockingOnTarget() && minecraft.options.keySprint.isDown() && !minecraft.options.keyUse.isDown()) {
            cir.setReturnValue(player.getYRot());
        }
    }

    @Inject(
            method = "setNextLockOnTarget(IZZ)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void onSetNextLockOnTarget(int direction, boolean necessarilyLockingOn, boolean sendChange, CallbackInfoReturnable<Boolean> cir){
        cir.cancel();

        if (!this.lockingOnTarget && necessarilyLockingOn) {
            cir.setReturnValue(false);
            return;
        }

        List<Entity> entitiesInLevel = new ArrayList<>();
        this.minecraft.level.entitiesForRendering().forEach(entitiesInLevel::add);
        Vec3 cameraLocation = this.minecraft.gameRenderer.getMainCamera().getPosition();

        Matrix4f compactProjection = this.getCompactProjectionMatrix();
        double lockOnRange = LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get();

        Optional<Pair<LivingEntity, Float>> next = entitiesInLevel.stream()
                .filter(entity ->
                        this.predicateFocusableEntity(entity) &&
                                (entity.getTeam() == null || entity.getTeam() != this.minecraft.player.getTeam()) &&
                                !entity.is(this.focusingEntity) &&
                                MathUtils.canBeSeen(entity, this.minecraft.player, lockOnRange) &&
                                this.minecraft.getEntityRenderDispatcher().shouldRender(entity, this.minecraft.levelRenderer.getFrustum(), cameraLocation.x(), cameraLocation.y(), cameraLocation.z()) &&
                                !entity.hasIndirectPassenger(this.minecraft.player)	&&
                                entity.distanceToSqr(this.minecraft.player) < lockOnRange * lockOnRange
                )
                .map(entity -> Pair.of((LivingEntity)entity, MathUtils.worldToScreenCoord(compactProjection, this.minecraft.gameRenderer.getMainCamera(), entity.getBoundingBox().getCenter()).x))
                .filter(pair -> pair.getSecond() >= -1.0F && pair.getSecond() <= 1.0F && (direction == 0 || MathUtils.getSign(pair.getSecond()) == MathUtils.getSign(direction)))
                .min((p1, p2) -> Float.compare(Math.abs(p1.getSecond()), Math.abs(p2.getSecond())));

        next.ifPresent(pair -> {
            this.focusingEntity = pair.getFirst();
            if (sendChange) this.sendTargeting(this.focusingEntity);
        });

        cir.setReturnValue(next.isPresent());
    }


    @Inject(
            method = "preClientTick",
            at = @At(value = "HEAD")
    )
    private void onPreClientTick(CallbackInfo ci){
        BLOCameraSetting.tick();

        if(blo$isAiming && blo$aimingTick < blo$maxAimingTick) {
            blo$aimingTick++;
        }
        else if(!blo$isAiming && blo$aimingTick > 0){
            blo$aimingTick--;
        }
    }

    @Inject(
            method = "setLockOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;setXRot(F)V"
            ),
            remap = true
    )
    private void onSetLockOn(CallbackInfo ci){
        this.minecraft.player.setYRot(cameraYRot);
        BLOCameraSetting.fovOffset = 0;
        BLOCameraSetting.setTransitionTick();
        BLOCameraSetting.setTargetOffset(0,0,0);
    }

    @Redirect(
            method = "setLockOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/client/camera/EpicFightCameraAPI;setCameraRotations(FFZ)V"
            )
    )
    private void onSetLockOn2(EpicFightCameraAPI instance, float xRot, float yRot, boolean syncOld){
        BLOCameraSetting.setTransitionTick();
        if(ModList.get().isLoaded(ShoulderSurfingCommon.MOD_ID)){
            HandlerShoulderSurfingCompat.handlerCam();
        }
        else {
            instance.setCameraRotations(xRot, yRot, syncOld);
        }
    }


    @Inject(
            method = "setLockOn",
            at = @At(value = "HEAD")
    )
    private void onSetLockOn3(boolean flag, CallbackInfo ci){
        if(flag && this.focusingEntity != null){
            this.focusingEntity = null;
        }
    }

    @Inject(
            method = "turnCamera",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void onTurnCamera(double dy, double dx, CallbackInfoReturnable<Boolean> cir){
        cir.cancel();

        MutableBoolean cancel = new MutableBoolean(false);
        EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            cancel.setValue(this.minecraft.options.getCameraType() != CameraType.FIRST_PERSON && (this.isTPSMode() || this.lockingOnTarget));

            if (cancel.booleanValue()) {
                float modifier = !this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY) ? 0.15F : (ClientConfig.lockOnQuickShift ? 0.005F : 0.0F);
                this.setCameraRotations(Mth.clamp(this.cameraXRot + (float)dx * modifier, -90.0F, 90.0F), this.cameraYRot + (float)dy * modifier, false);

                if (ClientConfig.lockOnQuickShift && this.quickShiftDelay <= 0) {
                    this.accumulatedX += -dy * 0.15F;

                    if (Math.abs(this.accumulatedX) > 20.0D && this.lockingOnTarget) {
                        this.setNextLockOnTarget(Mth.sign(this.accumulatedX), true, true);
                        this.accumulatedX = 0.0D;
                        this.quickShiftDelay = 4;
                    }
                }

                this.accumulatedX *= 0.98D;
            }
        });

        cir.setReturnValue(cancel.booleanValue());
    }

    @Inject(
            method = "postClientTick",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void rewroteClientTick(CallbackInfo ci){
        ci.cancel();

        if (this.minecraft.isPaused() || this.minecraft.player == null) return;

        EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            CapabilityItem mainHandItemCap = playerpatch.getAdvancedHoldingItemCapability(InteractionHand.MAIN_HAND);
            CapabilityItem offhandItemCap = playerpatch.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
            CapabilityItem.ZoomInType rangeWeaponZoomInType =
                    mainHandItemCap.isEmpty() || mainHandItemCap.getZoomInType() == CapabilityItem.ZoomInType.NONE
                            ? offhandItemCap.getZoomInType() : mainHandItemCap.getZoomInType();

            switch (rangeWeaponZoomInType) {
                case ALWAYS -> {
                    this.zoomIn();
                    blo$isAiming = true;
                }
                case USE_TICK -> {
                    blo$isAiming = playerpatch.getOriginal().getUseItemRemainingTicks() > 0;
                }
                case AIMING -> {
                    blo$isAiming = playerpatch.getClientAnimator().isAiming();
                }
                case CUSTOM -> {
                    blo$isAiming = true;
                }
                default -> {
                    this.zoomOut(1);
                    blo$isAiming = false;
                }
            }
        });

        double pickRange = this.minecraft.options.renderDistance().get() * 16.0D;
        Camera mainCamera = this.minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = mainCamera.getPosition();
        Vec3 lookVec = new Vec3(mainCamera.getLookVector());
        Vec3 rayEed = cameraPos.add(lookVec.x * pickRange, lookVec.y * pickRange, lookVec.z * pickRange);
        LocalPlayer localPlayer = this.minecraft.player;
        this.crosshairHitResult = localPlayer.level().clip(new ClipContext(cameraPos, rayEed, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, localPlayer));

        //最远索敌距离
        double entityPickRange = LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get() * LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get();

        AABB aabb = localPlayer.getBoundingBox().move(cameraPos.subtract(localPlayer.getEyePosition(1.0F))).expandTowards(lookVec.scale(entityPickRange)).inflate(1.0D, 1.0D, 1.0D);

        EntityHitResult crosshairResult = ProjectileUtil.getEntityHitResult(localPlayer, cameraPos, rayEed, aabb, entity->{
            return !entity.isSpectator() && entity.isPickable() && entity.isAlive() && !entity.is(this.minecraft.player);
        }, entityPickRange);

        if(crosshairResult != null) {
            this.crosshairHitResult = crosshairResult;
        }


        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(localPlayer, cameraPos, rayEed, aabb, this::predicateFocusableEntity, entityPickRange);
        if (entityHitResult != null) {

            if (!entityHitResult.getEntity().is(this.focusingEntity)) {
                if (entityHitResult.getEntity() instanceof LivingEntity livingentity) {
                    if (!(entityHitResult.getEntity() instanceof ArmorStand) && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
                        this.focusingEntity = livingentity;
                    }
                } else if (entityHitResult.getEntity() instanceof PartEntity<?> partEntity) {
                    Entity parent = partEntity.getParent();

                    if (parent instanceof LivingEntity parentLivingEntity && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
                        this.focusingEntity = parentLivingEntity;
                    }
                } else {
                    this.setLockOn(false);
                    this.focusingEntity = null;
                }

                if (this.focusingEntity != null) {
                    this.sendTargeting(this.focusingEntity);
                }
            }
        }

        boolean tpsMode = this.isTPSMode();

        if (tpsMode) {
            Vec3 view = new Vec3(mainCamera.getLookVector());
            if (view.dot(this.crosshairHitResult.getLocation().subtract(localPlayer.getEyePosition()).normalize()) < -0.1D) {
                this.crosshairHitResult = BlockHitResult.miss(cameraPos.add(lookVec.x * 50.0D, lookVec.y * 50.0D, lookVec.z * 50.0D), Direction.UP, BlockPos.ZERO);

                if (!this.lockingOnTarget && this.focusingEntity != null) {
                    this.focusingEntity = null;
                    this.sendTargeting(null);
                }
            }
            if (this.focusingEntity != null) {
                double dot = view.dot(this.focusingEntity.getEyePosition().subtract(localPlayer.getEyePosition()));

                if (dot < -0.1D) {
                    if (!this.lockingOnTarget) {
                        this.focusingEntity = null;
                        this.sendTargeting(null);
                    }
                }
            }
        }

        if (this.focusingEntity != null) {
            if (this.lockingOnTarget && !this.focusingEntity.isAlive()) {
                boolean releaseLockOn = !ClientConfig.lockOnQuickShift || !this.setNextLockOnTarget(0, true, true);
                if (releaseLockOn) {
                    this.setLockOn(false);
                }
            }
            else {
                double distance = this.minecraft.player.distanceToSqr(this.focusingEntity.position());
                //最远锁定距离
                double maxLockOnDistance = LockOnConfig.MAX_LOCK_ON_DISTANCE.get() *  LockOnConfig.MAX_LOCK_ON_DISTANCE.get();//focusingRange * focusingRange;

                boolean canBeSeen = MathUtils.canBeSeen(this.focusingEntity, this.minecraft.player, maxLockOnDistance);
                if(canBeSeen) {
                    blo$unlockDelayTick = 0;
                }
                else if(blo$unlockDelayTick < blo$maxUnlockDelayTick){
                    blo$unlockDelayTick++;
                }


                if (this.focusingEntity.isInvisibleTo(localPlayer) || distance > maxLockOnDistance || blo$unlockDelayTick >= blo$maxUnlockDelayTick ||
                        !this.lockingOnTarget && this.focusingEntity.position().subtract(mainCamera.getPosition()).normalize()
                                .dot(new Vec3(mainCamera.getLookVector())) < Mth.clampedLerp(0.8D, 0.96D, Mth.inverseLerp(Mth.clamp(distance, 9.0D, 64.0D), 9.0D, 64.0D))) {
                    if (this.lockingOnTarget) {
                        this.setLockOn(false);
                    }

                    blo$unlockDelayTick = 0;
                    this.focusingEntity = null;
                    this.sendTargeting(null);
                }
            }
        }

        if (this.isFirstPerson() && this.isLerpingFpv()) {
            this.fpvLerpTick--;
            if (!this.isLerpingFpv()) {
                this.minecraft.player.setXRot(this.fpvXRot);
                this.minecraft.player.setYRot(this.fpvYRot);
            }
        }
        else if (!this.isTPSMode() && !this.lockingOnTarget) {
            // Sync camera rotation when camera coupled to player's view
            this.cameraXRot = this.minecraft.player.getXRot();
            this.cameraYRot = this.minecraft.player.getYRot();
        }
        else {
            @Nullable
            LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(localPlayer, LocalPlayerPatch.class);
            float minPitch = LockOnConfig.MAX_PITCH.get().floatValue();
            float maxPitch = LockOnConfig.MIN_PITCH.get().floatValue();
            float pitchOffset = LockOnConfig.PITCH_OFFSET.get().floatValue();

            float clamp = 30.0F;
            float desiredXRot = 0.0F;
            float desiredYRot = 0.0F;

            // Handle camera lock-on
            if (this.focusingEntity != null && this.lockingOnTarget && !this.isLerpingFpv() && !InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
                Vec3 lockEnd;
                Vec3 lockStart;

                if (tpsMode) {
                    double toTargetDistanceSqr = localPlayer.position().distanceToSqr(this.focusingEntity.position());
                    lockStart = MathUtils.lerpVector(localPlayer.getEyePosition(), cameraPos, (float)Mth.clampedMap(toTargetDistanceSqr, 1.0F, 18.0F, 0.2F, 1.0F));
                    lockEnd = MathUtils.lerpVector(this.focusingEntity.getEyePosition(), this.focusingEntity.getBoundingBox().getCenter(), (float)Mth.clampedMap(toTargetDistanceSqr, 0.0F, 18.0F, 0.5F, 1.0F));
                }
                else {
                    lockStart = localPlayer.getEyePosition();
                    lockEnd = this.focusingEntity.getEyePosition();
                }

                Vec3 toTarget = lockEnd.subtract(lockStart);
                float xRot = (float)MathUtils.getXRotOfVector(toTarget);
                float yRot = (float)MathUtils.getYRotOfVector(toTarget);
                float originalXRot = xRot;

                CameraType cameraType = this.minecraft.options.getCameraType();
                Vec3 cameraToTarget = lockEnd.subtract(cameraPos);

                if (!cameraType.isFirstPerson()) {
                    xRot = (float)MathUtils.getXRotOfVector(cameraToTarget);
                    xRot = Mth.clamp(xRot + pitchOffset, minPitch, maxPitch);
                }

                //动态调整摄像机位置和朝向
                if(cameraType == CameraType.THIRD_PERSON_BACK && LockOnConfig.ENABLE_DYNAMIC_CAMERA.get()){
                    float maxXRot = -10F;
                    float minXRot = -70F;
                    float progress =  Mth.clamp((maxXRot - originalXRot) / (maxXRot - minXRot), 0F, 1F);

                    float distance2D = new Vec2((float) localPlayer.position().x, (float) localPlayer.position().z)
                            .distanceToSqr(new Vec2((float) this.focusingEntity.position().x, (float) this.focusingEntity.position().z));

                    float distance = Mth.sqrt(distance2D);
                    float distanceWeight = Mth.clampedMap(distance, 0.5F, 4.0F, 0.0F, 1.0F);

                    if(LockOnConfig.ENABLE_DYNAMIC_FOV.get()){
                        BLOCameraSetting.fovOffset = (LockOnConfig.MAX_FOV_MULTIPLIER.get().floatValue() - 1) * progress * distanceWeight;
                    } else if (BLOCameraSetting.fovOffset != 0) {
                        BLOCameraSetting.fovOffset = 0;
                    }

                    float length = -progress * LockOnConfig.MAX_DYNAMIC_CAMERA_X.get().floatValue() * distanceWeight;
                    Vec3 horizontalForward = new Vec3(cameraToTarget.x, 0, cameraToTarget.z).normalize().scale(length);

                    distanceWeight = Mth.clampedMap(distance, 0F, 3.0F, 0.0F, 1.0F);
                    float distanceY = (float) (this.focusingEntity.getEyePosition().y - localPlayer.getEyePosition().y) * distanceWeight;
                    distanceY = Mth.clamp(distanceY, 0, LockOnConfig.MAX_DYNAMIC_CAMERA_Y.get().floatValue()) * progress;

                    float aimProgress = (float) blo$aimingTick / (float) blo$maxAimingTick;

                    Vec3f relocation = new Vec3f(ClientConfig.cameraHorizontalLocation * 0.2F, ClientConfig.cameraVerticalLocation * 0.2F, 0.0F).scale(aimProgress);
                    OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-yRot, Vec3f.Y_AXIS), relocation, relocation);

                    double cameraOffsetX = horizontalForward.x * (1 - aimProgress);
                    double cameraOffsetY = distanceY * (1 - aimProgress);
                    double cameraOffsetZ = horizontalForward.z * (1 - aimProgress);
                    if(!isTPSMode()){
                        cameraOffsetX += relocation.x;
                        cameraOffsetY += relocation.y;
                        cameraOffsetZ += relocation.z;
                    }

                    BLOCameraSetting.setTargetOffset((float) cameraOffsetX, (float) cameraOffsetY, (float) cameraOffsetZ);
                }
                else{
                    BLOCameraSetting.reset();
                }

                float xLerp = Mth.clamp(Mth.wrapDegrees(xRot - this.cameraXRot) * 0.4F, -clamp, clamp);
                float yLerp = Mth.clamp(Mth.wrapDegrees(yRot - this.cameraYRot) * 0.4F, -clamp, clamp);

                this.setCameraRotations(this.cameraXRot + xLerp, this.cameraYRot + yLerp, false);

                Vec3 playerToTarget = lockEnd.subtract(localPlayer.getEyePosition());
                desiredXRot = (float)MathUtils.getXRotOfVector(playerToTarget);
                desiredYRot = (float)MathUtils.getYRotOfVector(playerToTarget);
            }
            else if (this.lockingOnTarget && InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
                desiredXRot = this.cameraXRot;
                desiredYRot = this.cameraYRot;
            }
            else if (tpsMode) {
                CoupleTPSCamera coupleCameraEvent = this.predicateCouplingPlayer();
                boolean shouldCoupling = coupleCameraEvent.shouldCoupleCamera();
                if (Mth.abs(Mth.wrapDegrees(this.cameraYRot - localPlayer.yBodyRot)) <= 51.0F || shouldCoupling) {
                    if (coupleCameraEvent.isOnlyMoving()) {
                        Vec2 movemoventPulse = localPlayer.input.getMoveVector();
                        desiredYRot = this.cameraYRot + (float)MathUtils.getYRotOfVector(new Vec3(movemoventPulse.x, 0.0D, movemoventPulse.y));
                        desiredXRot = desiredYRot == this.cameraYRot ? this.cameraXRot : 0.0F;
                    }
                    else {
                        Vec3 toHitResult;
                        if (this.lockingOnTarget) {
                            toHitResult = this.focusingEntity.getEyePosition();
                        } else if (this.crosshairHitResult.getType() == HitResult.Type.MISS) {
                            double delta = Mth.clamp(localPlayer.getXRot(), -30.0F, 0.0F) / -30.0F;
                            double lookVecScale = Mth.clampedLerp(30.0D, 75.0D, delta);
                            toHitResult = cameraPos.add(lookVec.scale(lookVecScale));
                        } else {
                            toHitResult = this.crosshairHitResult.getLocation();
                        }

                        toHitResult = toHitResult.subtract(localPlayer.getEyePosition());
                        desiredXRot = (float)MathUtils.getXRotOfVector(toHitResult);
                        desiredYRot = shouldCoupling ? (Math.abs(this.cameraXRot) > 80.0F ? this.cameraYRot : (float)MathUtils.getYRotOfVector(toHitResult)) : this.cameraYRot;
                    }
                }
                else {
                    desiredXRot = 0.0F;
                    desiredYRot = localPlayer.yBodyRot;
                    clamp = 15.0F;
                }
            }

            if (this.focusingEntity != null && this.lockingOnTarget) {
                EpicFightCameraAPI cameraAPI = (EpicFightCameraAPI) (Object) this;
                LockOnEvent.Tick lockOnEventTick = new LockOnEvent.Tick(cameraAPI, this.focusingEntity, desiredXRot, desiredYRot);
                EpicFightClientHooks.Camera.LOCK_ON_TICK.post(lockOnEventTick);
                desiredXRot = lockOnEventTick.getModifiedXRot();
                desiredYRot = lockOnEventTick.getModifiedYRot();
            }

            if ((playerpatch == null || !playerpatch.getEntityState().turningLocked() || playerpatch.getEntityState().lockonRotate()) &&
                    (tpsMode || this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK && this.lockingOnTarget)) {

                float xDelta = Mth.clamp(Mth.wrapDegrees(desiredXRot - localPlayer.getXRot()), -clamp, clamp);
                float yDelta = Mth.wrapDegrees(desiredYRot - localPlayer.getYRot()) - blo$getOffset();

                if (isLockingOnTarget() && minecraft.options.keySprint.isDown() && !minecraft.options.keyUse.isDown()) {
                    localPlayer.setXRot(0);
                } else{
                    localPlayer.setXRot(localPlayer.getXRot() + xDelta);
                }

                localPlayer.setYRot(localPlayer.getYRot() + yDelta);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "setupCamera",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void rewroteSetupCamera(Camera camera, float partialTick, CallbackInfoReturnable<BuildCameraTransform.Pre> cir) {
        cir.cancel();
        EpicFightCameraAPI cameraAPI = (EpicFightCameraAPI) (Object) this;

        BuildCameraTransform.Pre event = new BuildCameraTransform.Pre(cameraAPI, camera, partialTick);
        if (!camera.getEntity().is(this.minecraft.player)) {
            event.cancel();

            cir.setReturnValue(event);
            return;
        }

        EpicFightClientHooks.Camera.BUILD_TRANSFORM_PRE.post(event);
        if (event.hasCanceled()) {
            cir.setReturnValue(event);
            return;
        }

        if (this.isTPSMode()) {
            float partialZoomTick = this.zoomTick == 0 ? 0.0F : Math.min(this.zoomTick + (this.zoomingIn ? partialTick : -partialTick), MAX_ZOOM_TICK - 1);
            float delta = ClientConfig.getCameraMode() == ClientConfig.TPSType.WHEN_AIMING ? partialZoomTick / (MAX_ZOOM_TICK - 1) : 1.0F;
            float xRot = Mth.rotLerp(delta, this.minecraft.player.getXRot(), Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot));
            float yRot = Mth.rotLerp(delta, this.minecraft.player.getYRot(), Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot));
            camera.setRotation(yRot, xRot);

            Vec3 cameraOffset = Vec3.ZERO;
            if(isLockingOnTarget()){
                cameraOffset = blo$getCameraOffset(partialTick);
            }

            Vec3 playerPos = new Vec3(
                    Mth.lerp(partialTick, camera.getEntity().xo, camera.getEntity().getX()),
                    Mth.lerp(partialTick, camera.getEntity().yo, camera.getEntity().getY()) +
                            Mth.lerp((double)partialTick, camera.eyeHeightOld, camera.eyeHeight),
                    Mth.lerp(partialTick, camera.getEntity().zo, camera.getEntity().getZ())
            );

            Vec3f relocation = new Vec3f(
                    ClientConfig.cameraHorizontalLocation * 0.2F,
                    ClientConfig.cameraVerticalLocation * 0.2F,
                    0.0F
            );
            OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-yRot, Vec3f.Y_AXIS), relocation, relocation);
            double cameraZoom = ClientConfig.cameraZoom * 0.5D - (partialZoomTick * 0.1D);
            double hitDistance = 1.0D;

            Vec3 baseOffset = new Vec3(
                    relocation.x - camera.getLookVector().x() * cameraZoom + cameraOffset.x,
                    relocation.y - camera.getLookVector().y() * cameraZoom + cameraOffset.y,
                    relocation.z - camera.getLookVector().z() * cameraZoom + cameraOffset.z
            );

            for (int i = 0; i < 8; ++i) {

                float f = (float)((i & 1) * 2 - 1) * 0.1F;
                float f1 = (float)((i >> 1 & 1) * 2 - 1) * 0.1F;
                float f2 = (float)((i >> 2 & 1) * 2 - 1) * 0.1F;

                Vec3 start = playerPos.add(f, f1, f2);
                Vec3 end = playerPos.add(baseOffset).add(f, f1, f2);

                double fullLength = start.distanceTo(end);

                HitResult hit = this.minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));

                if (hit.getType() != HitResult.Type.MISS) {
                    double ratio = hit.getLocation().distanceTo(start) / fullLength;

                    if (ratio < hitDistance) {
                        hitDistance = ratio;
                    }
                }
            }

            Vec3 finalPos = playerPos.add(baseOffset.scale(hitDistance));

            double nearestX = finalPos.x;
            double nearestY = finalPos.y;
            double nearestZ = finalPos.z;

            if (Float.compare(1.0F, delta) == 0) {
                camera.setPosition(nearestX, nearestY, nearestZ);
            }
            else {
                camera.setRotation(this.minecraft.player.getViewYRot(partialTick), this.minecraft.player.getViewXRot(partialTick));

                camera.setPosition(
                        Mth.lerp(partialTick, this.minecraft.player.xo, this.minecraft.player.getX()),
                        Mth.lerp(partialTick, this.minecraft.player.yo, this.minecraft.player.getY()) +
                                Mth.lerp(partialTick, camera.eyeHeightOld, camera.eyeHeight),
                        Mth.lerp(partialTick, this.minecraft.player.zo, this.minecraft.player.getZ())
                );

                camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
                camera.setRotation(yRot, xRot);
                camera.setPosition(
                        camera.getPosition().x() + (nearestX - camera.getPosition().x()) * delta,
                        camera.getPosition().y() + (nearestY - camera.getPosition().y()) * delta,
                        camera.getPosition().z() + (nearestZ - camera.getPosition().z()) * delta
                );
            }

            event.setVanillaCameraSetupCanceled(true);
            this.fireCameraBuildPost(camera, partialTick);

            cir.setReturnValue(event);
            return;
        }
        else if((!BLOCameraSetting.transitionFinished() && this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) ||(this.lockingOnTarget && this.focusingEntity != null)){
            if (this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                float xRot = Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot);
                float yRot = Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot);

                camera.setRotation(yRot, xRot);

                Vec3 playerPos = new Vec3(
                        Mth.lerp(partialTick, camera.getEntity().xo, camera.getEntity().getX()),
                        Mth.lerp(partialTick, camera.getEntity().yo, camera.getEntity().getY())
                                + Mth.lerp(partialTick, camera.eyeHeightOld, camera.eyeHeight),
                        Mth.lerp(partialTick, camera.getEntity().zo, camera.getEntity().getZ())
                );

                Vec3 cameraOffset = blo$getCameraOffset(partialTick);
                Vec3 desiredPos = playerPos.add(cameraOffset.x, cameraOffset.y, cameraOffset.z);

                double hitDistance = 1.0D;
                for (int i = 0; i < 8; ++i) {
                    float f = (float)((i & 1) * 2 - 1);
                    float f1 = (float)((i >> 1 & 1) * 2 - 1);
                    float f2 = (float)((i >> 2 & 1) * 2 - 1);

                    f *= 0.1F;
                    f1 *= 0.1F;
                    f2 *= 0.1F;

                    Vec3 start = playerPos.add(f, f1, f2);
                    Vec3 end = desiredPos.add(f, f1, f2);

                    double fullLength = start.distanceTo(end);
                    HitResult hit = this.minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));

                    if (hit.getType() != HitResult.Type.MISS) {
                        double ratio = hit.getLocation().distanceTo(start) / fullLength;

                        if (ratio < hitDistance) {
                            hitDistance = ratio;
                        }
                    }
                }

                Vec3 finalPos = playerPos.add(new Vec3(cameraOffset.x, cameraOffset.y, cameraOffset.z).scale(hitDistance));

                camera.setRotation(yRot, xRot);
                camera.setPosition(finalPos.x, finalPos.y, finalPos.z);


                if (camera.isDetached()) {
                    camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
                }
                else if (camera.getEntity() instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
                    Direction direction = ((LivingEntity)camera.getEntity()).getBedOrientation();
                    camera.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
                    camera.move(0.0D, 0.3D, 0.0D);
                }

                event.setVanillaCameraSetupCanceled(true);
                this.fireCameraBuildPost(camera, partialTick);

                cir.setReturnValue(event);
                return;
            }
            else if (this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
                if (!InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
                    camera.getEntity().setXRot(Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot));
                    camera.getEntity().setYRot(Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot));
                }
                else {
                    this.cameraXRot = camera.getEntity().getXRot();
                    this.cameraYRot = camera.getEntity().getYRot();
                }
            }
        }

        cir.setReturnValue(event);
    }
}
