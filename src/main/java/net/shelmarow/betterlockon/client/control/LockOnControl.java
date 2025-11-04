package net.shelmarow.betterlockon.client.control;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.betterlockon.BetterLockOn;
import net.shelmarow.betterlockon.config.LockOnConfig;
import net.shelmarow.betterlockon.mixins.LocalPlayerPatchAccessor;
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
                    accessor.setRayTarget(changedTarget);
                    EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(changedTarget.getId()));
                    playerPatch.setLockOn(true);
                    lastMovedDistanceX = 0;
                    lastMovedDistanceY = 0;
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
            if (canBeSeenAsTarget(frustum,player,entity,target,level,deltaMouseX)) {
                livingEntities.add((LivingEntity) entity);
            }
        }
        return livingEntities;
    }


    //整合条件
    private static boolean canBeSeenAsTarget(Frustum frustum, LocalPlayer player, Entity entity, LivingEntity target, ClientLevel level, double deltaMouseX) {
        int delta = getEntitySide(player,entity);
        double maxRange = LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get();
        boolean valid = (entity instanceof Mob || entity instanceof Player)
                && entity.isAlive()
                && !entity.isInvisibleTo(player)
                && !entity.getUUID().equals(player.getUUID())
                && player.canAttack(target,TargetingConditions.forCombat())
                && player.distanceToSqr(entity) < maxRange * maxRange
                && frustum.isVisible(entity.getBoundingBox())
                && isUnobstructed(level, (LivingEntity) entity)
                && (deltaMouseX == 0 || delta == deltaMouseX);

        if (target != null) {
            valid = valid
                    && !entity.getUUID().equals(target.getUUID())
                    && player.canAttack(target, TargetingConditions.forCombat())
                    && target.canBeSeenAsEnemy();
        }

        return valid;
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
    private static boolean isUnobstructed(ClientLevel level, LivingEntity entity){
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
