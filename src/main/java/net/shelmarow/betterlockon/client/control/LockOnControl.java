package net.shelmarow.betterlockon.client.control;

import com.github.exopandora.shouldersurfing.ShoulderSurfingCommon;
import com.github.exopandora.shouldersurfing.api.model.Perspective;
import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.betterlockon.BetterLockOn;
import net.shelmarow.betterlockon.config.LockOnConfig;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.config.ClientConfig;

@Mod.EventBusSubscriber(modid = BetterLockOn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LockOnControl {

    private static CameraType lastCameraType = CameraType.FIRST_PERSON;
    private static boolean shouldSurfing = false;

    @SubscribeEvent
    public static void movementInputUpdateEvent(MovementInputUpdateEvent event) {
        Minecraft MC = Minecraft.getInstance();
        Input input = event.getInput();
        CameraType cameraType = MC.options.getCameraType();
        EpicFightCameraAPI instance = EpicFightCameraAPI.getInstance();
        if (MC.options.keySprint.isDown() && !MC.options.keyUse.isDown()) {
            if(instance.isLockingOnTarget() && cameraType == CameraType.THIRD_PERSON_BACK){
                if(input.forwardImpulse < 0){
                    input.forwardImpulse = -input.forwardImpulse;
                }
                if(Math.abs(input.leftImpulse) > 0){
                    input.forwardImpulse = Math.abs(input.leftImpulse);
                    input.leftImpulse = 0;
                }
            }
        }
    }

    public static void handleCamera(Camera camera, boolean isTps, boolean isLockingOnTarget, float yRot, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(minecraft.level != null && player != null && camera.getEntity() == player){
            CameraType cameraType = minecraft.options.getCameraType();
            double distanceToCamera = camera.getPosition().distanceToSqr(camera.getEntity().getEyePosition());
            double changeDistance = LockOnConfig.AUTO_SWITCH_DISTANCE.get();
            if(cameraType == CameraType.THIRD_PERSON_BACK && distanceToCamera > 0.05 && distanceToCamera < changeDistance * changeDistance){
                if(LockOnConfig.AUTO_SWITCH_FIRST_PERSON.get()){
                    shouldSurfing = ModList.get().isLoaded(ShoulderSurfingCommon.MOD_ID) && ShoulderSurfingImpl.getInstance().isShoulderSurfing();
                    if(EpicFightCameraAPI.getInstance().isTPSMode()){
                        player.setYRot(camera.getYRot());
                        player.setXRot(camera.getXRot());
                    }
                    minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                }
                lastCameraType = cameraType;
            }
            else if(cameraType.isFirstPerson() && !lastCameraType.isFirstPerson()){
                double zoom = shouldSurfing ? camera.getMaxZoom(4) : (isTps ? tpsCameraPos(camera, isLockingOnTarget, yRot, partialTick) : camera.getMaxZoom(4));
                if(zoom >= changeDistance + 0.5){
                    if(LockOnConfig.AUTO_SWITCH_FIRST_PERSON.get()){
                        if(shouldSurfing){
                            ShoulderSurfingImpl.getInstance().changePerspective(Perspective.SHOULDER_SURFING);
                        }
                        else {
                            minecraft.options.setCameraType(lastCameraType);
                        }
                    }
                    lastCameraType = cameraType;
                }
            }
        }
    }

    public static double thirdPersonPos(Camera camera, float partialTick){
        Minecraft minecraft = Minecraft.getInstance();

        Vec3 playerPos = new Vec3(
                Mth.lerp(partialTick, camera.getEntity().xo, camera.getEntity().getX()),
                Mth.lerp(partialTick, camera.getEntity().yo, camera.getEntity().getY())
                        + Mth.lerp(partialTick, camera.eyeHeightOld, camera.eyeHeight),
                Mth.lerp(partialTick, camera.getEntity().zo, camera.getEntity().getZ())
        );

        Vec3 cameraOffset = BLOCameraSetting.getCameraPos(partialTick);
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
            HitResult hit = minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));

            if (hit.getType() != HitResult.Type.MISS) {
                double ratio = hit.getLocation().distanceTo(start) / fullLength;

                if (ratio < hitDistance) {
                    hitDistance = ratio;
                }
            }
        }

        return new Vec3(cameraOffset.x, cameraOffset.y, cameraOffset.z).scale(hitDistance).length();
    }

    public static double tpsCameraPos(Camera camera, boolean isLockingOnTarget, float yRot, float partialTick){
        Minecraft minecraft =  Minecraft.getInstance();

        Vec3 cameraOffset = Vec3.ZERO;
        if(isLockingOnTarget || !BLOCameraSetting.transitionFinished()){
            cameraOffset = BLOCameraSetting.getCameraPos(partialTick);
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
        double cameraZoom = ClientConfig.cameraZoom * 0.5D;
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

            HitResult hit = minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));

            if (hit.getType() != HitResult.Type.MISS) {
                double ratio = hit.getLocation().distanceTo(start) / fullLength;

                if (ratio < hitDistance) {
                    hitDistance = ratio;
                }
            }
        }

        return baseOffset.scale(hitDistance).length();
    }

    public static float getModelAlpha(double distance){
        float start = LockOnConfig.START_TRANSPARENCY_DISTANCE.get().floatValue();
        float end = LockOnConfig.FULLY_TRANSPARENCY_DISTANCE.get().floatValue();
        if(LockOnConfig.ENABLE_MODEL_TRANSPARENCY.get() && distance <= start){
            return (float) Mth.clampedMap(distance , end, start, 0.0F, 1.0F);
        }
        return 1F;
    }
}
