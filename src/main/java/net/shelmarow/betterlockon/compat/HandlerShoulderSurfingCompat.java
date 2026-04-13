package net.shelmarow.betterlockon.compat;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

public class HandlerShoulderSurfingCompat {

    public static void handlerCam(){
        ShoulderSurfingImpl surfing = ShoulderSurfingImpl.getInstance();
        if (surfing.isShoulderSurfing()) {
            EpicFightCameraAPI api = EpicFightCameraAPI.getInstance();
            api.setCameraRotations(surfing.getCamera().getXRot(), surfing.getCamera().getYRot(), true);
        }
    }
}
