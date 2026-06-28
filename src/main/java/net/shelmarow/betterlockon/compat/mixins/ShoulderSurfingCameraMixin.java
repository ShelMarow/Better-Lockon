package net.shelmarow.betterlockon.compat.mixins;

import com.github.exopandora.shouldersurfing.api.math.Vec2f;
import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import net.shelmarow.betterlockon.client.control.BLOCameraSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class ShoulderSurfingCameraMixin {

    @Shadow private Vec2f rotation;
    @Shadow private Vec2f rotationOffset;
    @Shadow private Vec2f rotationOffsetO;

    @Shadow private Vec2f rotationO;

    @Shadow private Vec2f renderRotation;

    @Inject(
            method = "setXRot",
            at = @At(value = "HEAD"),
            cancellable = true
    )

    private void setXRot(float xRot, CallbackInfo ci){
        ci.cancel();
        this.rotation = new Vec2f(xRot, this.rotation.y());
        this.rotationO = new Vec2f(xRot, this.rotationO.y());
        this.rotationOffset = new Vec2f(0, this.rotationOffset.y());
        this.rotationOffsetO = new Vec2f(0, this.rotationOffsetO.y());
    }

    @Inject(
            method = "setYRot",
            at = @At(value = "HEAD"),
            cancellable = true
    )

    private void setYRot(float yRot, CallbackInfo ci){
        ci.cancel();
        this.rotation = new Vec2f(this.rotation.x(), yRot);
        this.rotationO = new Vec2f(this.rotationO.y(), yRot);
        this.rotationOffset = new Vec2f(this.rotationOffset.x(), 0);
        this.rotationOffsetO = new Vec2f(this.rotationOffsetO.x(), 0);
    }
}

