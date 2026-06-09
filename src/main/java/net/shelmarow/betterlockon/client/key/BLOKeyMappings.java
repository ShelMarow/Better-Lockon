package net.shelmarow.betterlockon.client.key;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shelmarow.betterlockon.BetterLockOn;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BetterLockOn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BLOKeyMappings {


    public static final KeyMapping CHANGE_JOINT = creatKey("change_lock_on_joint", GLFW.GLFW_KEY_GRAVE_ACCENT);


    private static KeyMapping creatKey(String name, int key){
        return new KeyMapping("key."+  BetterLockOn.MOD_ID + "." + name, key, "key."+ BetterLockOn.MOD_ID +".category");
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_JOINT);
    }
}
