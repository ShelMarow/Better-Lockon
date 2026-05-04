package net.shelmarow.betterlockon.client.control;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.betterlockon.BetterLockOn;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mod.EventBusSubscriber(modid = BetterLockOn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LockOnControl {
    private static final Minecraft MC = Minecraft.getInstance();

    @SubscribeEvent
    public static void movementInputUpdateEvent(MovementInputUpdateEvent event) {
        Input input = event.getInput();
        CameraType cameraType = MC.options.getCameraType();
        if (MC.options.keySprint.isDown() && !MC.options.keyUse.isDown() && EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
            if(cameraType == CameraType.THIRD_PERSON_BACK){
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
}
