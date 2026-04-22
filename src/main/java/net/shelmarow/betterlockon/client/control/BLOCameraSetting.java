package net.shelmarow.betterlockon.client.control;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BLOCameraSetting {
    //摄像机位置
    private static float cameraOffsetX;
    private static float cameraOffsetXO;
    private static float cameraOffsetY;
    private static float cameraOffsetYO;
    private static float cameraOffsetZ;
    private static float cameraOffsetZO;
    private static float targetOffsetX;
    private static float targetOffsetY;
    private static float targetOffsetZ;
    //暂无用处的偏移
    private static float cameraBaseOffsetX;
    private static float cameraBaseOffsetY;
    private static float cameraBaseOffsetZ;

    //FOV调整
    public static float fovOffset;

    //相机位置过渡
    private static float smoothSpeed = 0.15f;
    private static final int maxTransitionTick = 30;
    private static int transitionTick = maxTransitionTick;

    public static void reset() {
        fovOffset = 0F;
        targetOffsetX = 0;
        targetOffsetY = 0;
        targetOffsetZ = 0;
        cameraOffsetX = 0;
        cameraOffsetY = 0;
        cameraOffsetZ = 0;
        cameraOffsetXO = cameraOffsetX;
        cameraOffsetYO = cameraOffsetY;
        cameraOffsetZO = cameraOffsetZ;
    }

    public static void tick() {
        cameraOffsetXO = cameraOffsetX;
        cameraOffsetYO = cameraOffsetY;
        cameraOffsetZO = cameraOffsetZ;

        if (transitionTick < maxTransitionTick) {
            float progress = Mth.clampedMap(transitionTick++, maxTransitionTick * 2F / 3F, maxTransitionTick,0.25F,1F);
            cameraOffsetX = Mth.lerp(progress, cameraOffsetX, targetOffsetX);
            cameraOffsetY = Mth.lerp(progress, cameraOffsetY, targetOffsetY);
            cameraOffsetZ = Mth.lerp(progress, cameraOffsetZ, targetOffsetZ);
        } else {
            cameraOffsetX = targetOffsetX;
            cameraOffsetY = targetOffsetY;
            cameraOffsetZ = targetOffsetZ;
        }
    }

    public static void setTargetOffset(float x, float y, float z) {
        targetOffsetX = x;
        targetOffsetY = y;
        targetOffsetZ = z;
    }


    public static Vec3 getCameraPos(float partialTick) {
        float offsetX = Mth.lerp(partialTick, cameraOffsetXO, cameraOffsetX);
        float offsetY = Mth.lerp(partialTick, cameraOffsetYO, cameraOffsetY);
        float offsetZ = Mth.lerp(partialTick, cameraOffsetZO, cameraOffsetZ);
        return new Vec3(offsetX, offsetY, offsetZ);
    }



    public static float getCameraOffsetX() {
        return cameraOffsetX;
    }

    public static void setCameraOffsetX(float cameraOffsetX) {
        BLOCameraSetting.cameraOffsetX = cameraOffsetX;
    }

    public static float getCameraOffsetXO() {
        return cameraOffsetXO;
    }

    public static void setCameraOffsetXO(float cameraOffsetXO) {
        BLOCameraSetting.cameraOffsetXO = cameraOffsetXO;
    }

    public static float getCameraOffsetY() {
        return cameraOffsetY;
    }

    public static void setCameraOffsetY(float cameraOffsetY) {
        BLOCameraSetting.cameraOffsetY = cameraOffsetY;
    }

    public static float getCameraOffsetYO() {
        return cameraOffsetYO;
    }

    public static void setCameraOffsetYO(float cameraOffsetYO) {
        BLOCameraSetting.cameraOffsetYO = cameraOffsetYO;
    }

    public static float getCameraOffsetZ() {
        return cameraOffsetZ;
    }

    public static void setCameraOffsetZ(float cameraOffsetZ) {
        BLOCameraSetting.cameraOffsetZ = cameraOffsetZ;
    }

    public static float getCameraOffsetZO() {
        return cameraOffsetZO;
    }

    public static void setCameraOffsetZO(float cameraOffsetZO) {
        BLOCameraSetting.cameraOffsetZO = cameraOffsetZO;
    }

    public static float getCameraBaseOffsetX() {
        return cameraBaseOffsetX;
    }

    public static void setCameraBaseOffsetX(float cameraBaseOffsetX) {
        BLOCameraSetting.cameraBaseOffsetX = cameraBaseOffsetX;
    }

    public static float getCameraBaseOffsetY() {
        return cameraBaseOffsetY;
    }

    public static void setCameraBaseOffsetY(float cameraBaseOffsetY) {
        BLOCameraSetting.cameraBaseOffsetY = cameraBaseOffsetY;
    }

    public static float getCameraBaseOffsetZ() {
        return cameraBaseOffsetZ;
    }

    public static void setCameraBaseOffsetZ(float cameraBaseOffsetZ) {
        BLOCameraSetting.cameraBaseOffsetZ = cameraBaseOffsetZ;
    }

    public static float getTargetOffsetX() {
        return targetOffsetX;
    }

    public static void setTargetOffsetX(float targetOffsetX) {
        BLOCameraSetting.targetOffsetX = targetOffsetX;
    }

    public static float getTargetOffsetY() {
        return targetOffsetY;
    }

    public static void setTargetOffsetY(float targetOffsetY) {
        BLOCameraSetting.targetOffsetY = targetOffsetY;
    }

    public static float getTargetOffsetZ() {
        return targetOffsetZ;
    }

    public static void setTargetOffsetZ(float targetOffsetZ) {
        BLOCameraSetting.targetOffsetZ = targetOffsetZ;
    }

    public static float getSmoothSpeed() {
        return smoothSpeed;
    }

    public static void setSmoothSpeed(float smoothSpeed) {
        BLOCameraSetting.smoothSpeed = smoothSpeed;
    }

    public static int getTransitionTick() {
        return transitionTick;
    }

    public static void setTransitionTick() {
        transitionTick = 0;
    }


    public static boolean transitionFinished() {
        return transitionTick >= maxTransitionTick;
    }

}
