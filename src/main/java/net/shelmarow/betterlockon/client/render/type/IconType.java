package net.shelmarow.betterlockon.client.render.type;

import net.minecraft.resources.ResourceLocation;

public abstract class IconType {
    public abstract ResourceLocation getBackground();
    public abstract ResourceLocation getOverlay();
    public abstract ResourceLocation getHealth();
    public abstract Double getHealthStartAngle();
    public abstract Double getHealthTotalAngle();
    public abstract ResourceLocation getStamina();
}
