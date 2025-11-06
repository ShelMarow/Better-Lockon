package net.shelmarow.betterlockon.compat;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import net.shelmarow.betterlockon.mixins.LocalPlayerPatchAccessor;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public class HandlerShoulderSurfingCompat {
    public static void handlerCam(){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if(playerPatch != null){
            ShoulderSurfingImpl surfing = ShoulderSurfingImpl.getInstance();
            if (surfing.isCameraDecoupled()) {
                LocalPlayerPatchAccessor accessor = (LocalPlayerPatchAccessor) playerPatch;
                accessor.setLockOnXRot(surfing.getCamera().getXRot());
                accessor.setLockOnYRot(surfing.getCamera().getYRot());
            }
        }
    }

}
