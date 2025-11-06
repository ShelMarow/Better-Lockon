package net.shelmarow.betterlockon.client.control;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.shelmarow.betterlockon.BetterLockOn;
import net.shelmarow.betterlockon.config.LockOnConfig;
import net.shelmarow.betterlockon.mixins.LocalPlayerPatchAccessor;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = BetterLockOn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LockOnControl {
    private static final Minecraft MC = Minecraft.getInstance();
    //开始移动的时间
    private static int startTick = 0;
    //记录上一tick的坐标
    private static double lastMouseX = 0;
    private static double lastMouseY = 0;
    //记录上一tick的偏移方向
    private static double deltaMouseX = 0;
    //累计移动的距离
    private static double movedDistance = 0;
    private static double lastMovedDistanceX = 0;
    private static double lastMovedDistanceY = 0;
    // 冷却时间
    private static final int maxCoolDown = 1;
    private static int cooldownRemaining = 0;

    @SubscribeEvent
    public static void movementInputUpdateEvent(MovementInputUpdateEvent event) {
        Input input = event.getInput();
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (Minecraft.getInstance().options.keySprint.isDown()&& !Minecraft.getInstance().options.keyUse.isDown() && playerPatch != null && playerPatch.isTargetLockedOn()) {
            if(input.forwardImpulse < 0){
                input.forwardImpulse = -input.forwardImpulse;
            }
            if(Math.abs(input.leftImpulse) > 0){
                input.forwardImpulse = Math.abs(input.leftImpulse);
                input.leftImpulse = 0;
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if(event.side.isClient() && MC.screen == null && MC.player != null && event.player == MC.player && MC.level != null && event.phase == TickEvent.Phase.START){

            if (cooldownRemaining > 0) {
                cooldownRemaining--;
                lastMouseX = MC.mouseHandler.xpos();
                return;
            }

            double currentMouseX = MC.mouseHandler.xpos();
            double distanceX = currentMouseX - lastMouseX;
            double currentDeltaMouseX = distanceX == 0 ? 0 : (distanceX > 0 ? 1 : -1);
            lastMovedDistanceX += Mth.wrapDegrees(distanceX/30);

            double currentMouseY = MC.mouseHandler.ypos();
            double distanceY = currentMouseY - lastMouseY;
            lastMovedDistanceY += Mth.wrapDegrees(distanceY/30);

            movedDistance += Math.abs(distanceX);
            double mouseSpeed = movedDistance / (MC.player.tickCount - startTick + 1);

            //配置数据
            double changeDistance = LockOnConfig.LOCK_ON_CHANGE_DISTANCE.get();
            double minMoveSpeed = LockOnConfig.LOCK_ON_MIN_MOUSE_SPEED.get();
            double maxSoftAngleX = LockOnConfig.MAX_SOFT_ANGLE_X.get();
            double maxSoftAngleY = LockOnConfig.MAX_SOFT_ANGLE_Y.get();
            double md = LockOnConfig.CHANGE_DISTANCE_MULTIPLY.get();
            double ms = LockOnConfig.CHANGE_SPEED_MULTIPLY.get();

            if(LockOnConfig.ENABLE_SOFT_LOCK.get()){
                minMoveSpeed *= ms;
                changeDistance *= md;
            }

            if(lastMovedDistanceX > 0) {
                lastMovedDistanceX = Mth.clamp(lastMovedDistanceX, 0, maxSoftAngleX/2);
            }
            else if(lastMovedDistanceX < 0){
                lastMovedDistanceX = Mth.clamp(lastMovedDistanceX, -maxSoftAngleX/2, 0);
            }

            if(lastMovedDistanceY > 0) {
                lastMovedDistanceY = Mth.clamp(lastMovedDistanceY, 0, maxSoftAngleY/2);
            }
            else if(lastMovedDistanceY < 0){
                lastMovedDistanceY = Mth.clamp(lastMovedDistanceY, -maxSoftAngleY/2, 0);
            }

            if(currentMouseX == lastMouseX || (deltaMouseX !=0 && currentDeltaMouseX != deltaMouseX) || mouseSpeed < minMoveSpeed){
                movedDistance = 0;
                startTick = MC.player.tickCount;
            }

            LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(MC.player, LocalPlayerPatch.class);
            if (playerPatch != null && playerPatch.isTargetLockedOn()){
                if (movedDistance >= changeDistance) {
                    triggerTargetChange(MC.level,currentDeltaMouseX);
                    movedDistance = 0;
                    cooldownRemaining = maxCoolDown;
                }
            }
            else {
                movedDistance = 0;
            }

            deltaMouseX = currentDeltaMouseX;
            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
        }
    }

    public static float getLastMovedDistanceX() {
        return (float) lastMovedDistanceX;
    }

    public static void setLastMovedDistanceX(float distance) {
        lastMovedDistanceX = distance;
    }

    public static float getLastMovedDistanceY() {
        return (float) lastMovedDistanceY;
    }

    public static void setLastMovedDistanceY(double lastMovedDistanceY) {
        LockOnControl.lastMovedDistanceY = lastMovedDistanceY;
    }

    private static void triggerTargetChange(ClientLevel level, double deltaMouseX) {
        LocalPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(MC.player, LocalPlayerPatch.class);
        if (playerPatch != null) {
            LivingEntity target = playerPatch.getTarget();
            if(target != null && target.isAlive()){
                List<LivingEntity> targetList = entitiesCanBeSeen(MC.player,level,target,deltaMouseX);
                LivingEntity changedTarget = selectBestTarget(MC.player,targetList,target);

                LocalPlayerPatchAccessor accessor = (LocalPlayerPatchAccessor) playerPatch;
                if(changedTarget != null){
                    Vec3 playerPosition = MC.player.getEyePosition();
                    Vec3 targetPosition = changedTarget.getEyePosition();
                    Vec3 toTarget = targetPosition.subtract(playerPosition);
                    float yaw = (float) ((float) MathUtils.getYRotOfVector(toTarget));
                    float pitch = (float) ((float) MathUtils.getXRotOfVector(toTarget));

                    Vec3 preTargetPosition = target.getEyePosition();
                    Vec3 toPreTarget = preTargetPosition.subtract(playerPosition);

                    float deltaYaw = (float) (yaw - (float) MathUtils.getYRotOfVector(toPreTarget) - lastMovedDistanceX);
                    float deltaPitch = (float) (pitch - (float) MathUtils.getXRotOfVector(toPreTarget) - lastMovedDistanceY);


                    double maxSoftAngleX = LockOnConfig.MAX_SOFT_ANGLE_X.get();
                    double maxSoftAngleY = LockOnConfig.MAX_SOFT_ANGLE_Y.get();


                    if(deltaYaw >= -maxSoftAngleX / 2F && deltaYaw <= maxSoftAngleX / 2F){
                        lastMovedDistanceX = -deltaYaw - (deltaYaw > maxSoftAngleX / 2F ? lastMovedDistanceX : 0);
                    }
                    if(deltaYaw > 0 && deltaYaw > maxSoftAngleX / 2F){
                        lastMovedDistanceX = + maxSoftAngleX / 2F;
                    }
                    else if(deltaYaw < 0 && deltaYaw < -maxSoftAngleX / 2F){
                        lastMovedDistanceX = - maxSoftAngleX / 2F;
                    }

                    if(deltaPitch >= -maxSoftAngleY / 2F && deltaPitch <= maxSoftAngleY / 2F){
                        lastMovedDistanceY = -deltaPitch - (deltaPitch > maxSoftAngleY / 2F ? lastMovedDistanceY : 0);
                    }
                    else if(deltaPitch > 0 && deltaPitch > maxSoftAngleY / 2F){
                        lastMovedDistanceY = - maxSoftAngleY / 2F;
                    }
                    else if(deltaPitch < 0 && deltaPitch < -maxSoftAngleY / 2F){
                        lastMovedDistanceY = + maxSoftAngleY / 2F;
                    }

                    accessor.setRayTarget(changedTarget);
                    EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(changedTarget.getId()));
                    playerPatch.setLockOn(true);
                }
            }
        }
    }

    public static LivingEntity selectBestTarget(LocalPlayer player, List<LivingEntity> candidates, LivingEntity currentTarget) {
        if (candidates.isEmpty()) return null;

        // 标准距离：当前锁定目标与玩家的距离
        double standDistanceSqr = 100;
        if(currentTarget != null)standDistanceSqr = player.distanceToSqr(currentTarget);

        LivingEntity bestTarget = null;
        double bestWeight = Double.NEGATIVE_INFINITY;

        for (LivingEntity entity : candidates) {

            double weight = getWeight(player, entity, standDistanceSqr);

            if (weight > bestWeight) {
                bestWeight = weight;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    public static double getWeight(LocalPlayer player, LivingEntity entity,double standDistanceSqr) {

        Vec3 playerPos = player.position();
        Vec3 lookDir = player.getLookAngle().normalize();
        Vec3 toTarget = entity.position().subtract(playerPos).normalize();

        double dot = lookDir.dot(toTarget);
        dot = Math.max(-1.0, Math.min(1.0, dot));

        //角度：0°~180°
        double angleDegrees = Math.toDegrees(Math.acos(dot));
        //实际距离平方
        double distanceSqr = playerPos.distanceToSqr(entity.position());

        double totalWeight = 200;

        //角度权重：每 10° -5
        totalWeight -= angleDegrees * 0.5;

        //当距离大于标准距离时减权
        if (distanceSqr > standDistanceSqr) {
            double diff = Math.sqrt(distanceSqr) - Math.sqrt(standDistanceSqr);
            totalWeight -= diff * 2;
        }

        return totalWeight;
    }

    //获取视野范围内未被遮挡的实体
    public static List<LivingEntity> entitiesCanBeSeen(LocalPlayer player, ClientLevel level,LivingEntity target,double deltaMouseX) {
        Frustum frustum = MC.levelRenderer.getFrustum();
        List<LivingEntity> livingEntities = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity && canBeSeenAsTarget(frustum,player,livingEntity,target,level,deltaMouseX)) {
                livingEntities.add(livingEntity);
            }
        }
        return livingEntities;
    }


    //整合条件
    private static boolean canBeSeenAsTarget(Frustum frustum, LocalPlayer player, LivingEntity entity, LivingEntity target, ClientLevel level, double deltaMouseX) {
        int delta = getEntitySide(player, entity);
        double maxRange = LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get();
        double distanceSqr = player.distanceToSqr(entity);

        List<? extends String> whiteList = LockOnConfig.WHITE_LIST.get();
        List<? extends String> blackList = LockOnConfig.BLACK_LIST.get();

        String id = "";
        @Nullable ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (key != null) {
            id = key.toString();
        }

        //切换的目标不能是当前目标
        if(target != null && entity.getUUID().equals(target.getUUID())) return false;

        //其他条件
        if (!entity.isAlive()) return false;
        if (entity.isInvisibleTo(player)) return false;
        if (entity.getUUID().equals(player.getUUID())) return false;
        if (distanceSqr >= maxRange * maxRange) return false;
        if (!frustum.isVisible(entity.getBoundingBox())) return false;
        if (!isUnobstructed(level,entity)) return false;
        if (deltaMouseX != 0 && delta != deltaMouseX) return false;
        if(player.getVehicle() != null && entity == player.getVehicle()) return false;


        //筛选名单
        if(whiteList.contains(id)) return true;
        if(blackList.contains(id)) return false;

        if (entity instanceof Mob || entity instanceof Player) {
            return player.canAttack(entity, TargetingConditions.forCombat());
        }

        return false;
    }

    private static int getEntitySide(LocalPlayer player, Entity entity) {
        Vec3 playerPos = player.position();
        Vec3 lookDir = player.getLookAngle();
        Vec3 toTarget = entity.position().subtract(playerPos).normalize();
        Vec3 cross = lookDir.cross(toTarget);
        if (cross.y >= 0) return -1;
        else return 1;
    }

    //是否被方块或实体遮挡
    private static boolean isUnobstructed(ClientLevel level, Entity entity){
        if (MC.player == null) return false;

        Camera camera = MC.gameRenderer.getMainCamera();
        Vec3 startPos = MC.player.getEyePosition();
        Vec3 targetPos = entity.getEyePosition();

        // 方块遮挡检测
        ClipContext blockContext = new ClipContext(
                startPos,
                targetPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                MC.player
        );
        HitResult blockHit = level.clip(blockContext);
        if (blockHit.getType() == HitResult.Type.BLOCK)
            return false;

        // 实体遮挡检测
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                MC.player,
                startPos,
                targetPos,
                new AABB(startPos, targetPos).inflate(0.05),
                e -> e != MC.player && e.isAlive() && !e.isInvisibleTo(MC.player)
        );
        if (entityHit == null) {
            return true;
        }
        return entityHit.getEntity() == entity;
    }
}
