package net.shelmarow.betterlockon.client.render.icon.type;

import net.minecraft.resources.ResourceLocation;
import net.shelmarow.betterlockon.BetterLockOn;

public class RPGType1 extends IconType{
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/rpg_type1/lock_on_background.png");
    private final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/rpg_type1/lock_on_overlay.png");
    private final ResourceLocation RING = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/rpg_type1/lock_on_ring.png");
    //private final ResourceLocation STAMINA = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/rpg_type1/lock_on_stamina.png");

    @Override
    public String getName() {
        return "RPGType1";
    }

    @Override
    public ResourceLocation getBackground() {
        return this.BACKGROUND;
    }

    @Override
    public ResourceLocation getOverlay() {
        return this.OVERLAY;
    }

    @Override
    public ResourceLocation getHealth() {
        return this.RING;
    }

    @Override
    public Double getHealthStartAngle() {
        return Math.PI - Math.PI * 20 / 180;
    }

    @Override
    public Double getHealthTotalAngle() {
        return Math.PI * 195 / 180;
    }

    @Override
    public ResourceLocation getStamina() {
        return null;
    }

    @Override
    public boolean reverse() {
        return true;
    }
}
