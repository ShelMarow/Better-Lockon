package net.shelmarow.betterlockon;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.slf4j.Logger;

@Mod(BetterLockOn.MOD_ID)
public class BetterLockOn {
    public static final String MOD_ID = "betterlockon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterLockOn(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.CLIENT, LockOnConfig.CLIENT_CONFIG, "betterlockon_client.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}
}
