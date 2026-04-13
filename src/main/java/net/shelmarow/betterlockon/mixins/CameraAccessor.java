package net.shelmarow.betterlockon.mixins;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("eyeHeight")
    float betterlockon$getEyeHeight();

    @Accessor("eyeHeightOld")
    float betterlockon$getEyeHeightOld();

    @Invoker("getMaxZoom")
    float betterlockon$invokeGetMaxZoom(float distance);

    @Invoker("move")
    void betterlockon$invokeMove(float x, float y, float z);

    @Invoker("setRotation")
    void betterlockon$invokeSetRotation(float yRot, float xRot);

    @Invoker("setPosition")
    void betterlockon$invokeSetPosition(double x, double y, double z);
}
