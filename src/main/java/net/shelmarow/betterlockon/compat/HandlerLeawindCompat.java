package net.shelmarow.betterlockon.compat;

import com.github.leawind.thirdperson.ThirdPerson;
import net.minecraft.client.Minecraft;

public class HandlerLeawindCompat {

    public static float handlerDodgeRotation(float cameraYRot){
        if(ThirdPerson.isAvailable()){
            return (Minecraft.getInstance().gameRenderer.getMainCamera().getYRot());
        }
        return cameraYRot;
    }
}
