package net.shelmarow.betterlockon.client.render.icon.type;

import net.minecraft.resources.ResourceLocation;
import net.shelmarow.betterlockon.BetterLockOn;

public class DefaultType extends IconType{
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/default/lock_on_background.png");
    private final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/default/lock_on_overlay.png");
    private final ResourceLocation RING = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/default/lock_on_ring.png");
    private final ResourceLocation STAMINA = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/default/lock_on_stamina.png");

    @Override
    public String getName() {
        return "DefaultType";
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
        return Math.PI / 2;
    }

    @Override
    public Double getHealthTotalAngle() {
        return Math.PI * 2;
    }

    @Override
    public ResourceLocation getStamina() {
        return this.STAMINA;
    }

    @Override
    public boolean reverse() {
        return false;
    }
}
