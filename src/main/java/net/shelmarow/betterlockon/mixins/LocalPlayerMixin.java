package net.shelmarow.betterlockon.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    @Shadow
    public Input input;

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Redirect(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;hasForwardImpulse()Z")
    )
    public boolean aiStep(Input instance){
        if(EpicFightCameraAPI.getInstance().isTPSMode() && !EpicFightCameraAPI.getInstance().isLockingOnTarget()){
            return Math.abs(this.input.forwardImpulse) > 1.0E-5F || Math.abs(this.input.leftImpulse) > 1.0E-5F;
        }
        return instance.hasForwardImpulse();
    }

    @Inject(
            method = "hasEnoughImpulseToStartSprinting",
            at = @At("HEAD"),
            cancellable = true
    )
    public void hasEnoughImpulseToStartSprinting(CallbackInfoReturnable<Boolean> cir){
        if(EpicFightCameraAPI.getInstance().isTPSMode() && !EpicFightCameraAPI.getInstance().isLockingOnTarget()){
            boolean b = this.isUnderWater() ? input.hasForwardImpulse() : (Math.abs(this.input.forwardImpulse) >= 0.8D || Math.abs(this.input.leftImpulse) >= 0.8D);
            cir.setReturnValue(b);
        }
    }
}
