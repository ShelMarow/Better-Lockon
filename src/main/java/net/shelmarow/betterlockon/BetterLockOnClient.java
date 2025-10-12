package net.shelmarow.betterlockon;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.shelmarow.betterlockon.client.render.LockOnRenderer;

@Mod(value = BetterLockOn.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BetterLockOn.MOD_ID, value = Dist.CLIENT)
public class BetterLockOnClient {
    public BetterLockOnClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on the mod > clicking on config.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        //noinspection unused
        LockOnRenderer lockOnRenderer = new LockOnRenderer();
    }
}
