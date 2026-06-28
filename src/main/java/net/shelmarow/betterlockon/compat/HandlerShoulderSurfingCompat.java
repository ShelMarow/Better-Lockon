package net.shelmarow.betterlockon.compat;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfing;
import net.shelmarow.betterlockon.client.control.BLOCameraSetting;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

public class HandlerShoulderSurfingCompat {

    public static void setupCamera(){
        ShoulderSurfing surfing = ShoulderSurfing.getInstance();
        if (surfing.isShoulderSurfing()) {
            EpicFightCameraAPI api = EpicFightCameraAPI.getInstance();
            surfing.getCamera().setXRot(api.getCameraXRot());
            surfing.getCamera().setYRot(api.getCameraYRot());
        }
    }

    public static void setLockOn(){
        ShoulderSurfing surfing = ShoulderSurfing.getInstance();
        if (surfing.isShoulderSurfing()) {
            EpicFightCameraAPI api = EpicFightCameraAPI.getInstance();
            api.setCameraRotations(surfing.getCamera().getXRot(), surfing.getCamera().getYRot(), true);
        }
    }

    public static void disableLockOn() {
        BLOCameraSetting.setTransitionTick(BLOCameraSetting.getMaxTransitionTick());
        setupCamera();
    }
}
