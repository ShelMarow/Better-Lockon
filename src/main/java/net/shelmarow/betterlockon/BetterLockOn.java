package net.shelmarow.betterlockon;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shelmarow.betterlockon.client.render.LockOnRenderer;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.slf4j.Logger;

@Mod(BetterLockOn.MOD_ID)
public class BetterLockOn {
    public static final String MOD_ID = "betterlockon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BetterLockOn(FMLJavaModLoadingContext context){
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);


        context.registerConfig(ModConfig.Type.CLIENT, LockOnConfig.CLIENT_CONFIG,"betterlockon_client.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event){
        LockOnRenderer lockOnRenderer = new LockOnRenderer();
    }
}
