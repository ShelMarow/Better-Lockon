package net.shelmarow.betterlockon.compat;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.client.Minecraft;
import net.shelmarow.betterlockon.mixins.EpicFightCameraAPIAccessor;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class HandlerShoulderSurfingCompat {
    public static void handlerCam(){
        ShoulderSurfingImpl surfing = ShoulderSurfingImpl.getInstance();
        if (surfing.isCameraDecoupled()) {
            EpicFightCameraAPI api = EpicFightCameraAPI.getInstance();
            api.setCameraXRot(surfing.getCamera().getXRot());
            api.setCameraYRot(surfing.getCamera().getYRot());

        }
    }
}
