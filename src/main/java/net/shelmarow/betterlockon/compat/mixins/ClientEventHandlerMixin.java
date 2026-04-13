package net.shelmarow.betterlockon.compat.mixins;

import com.github.exopandora.shouldersurfing.neoforge.event.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerMixin {
    @Inject(
            method = "movementInputUpdateEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/exopandora/shouldersurfing/client/InputHandler;updateMovementInput(Lnet/minecraft/client/player/Input;)V"
            ),
            cancellable = true
    )
    private static void movementInputUpdateEvent(CallbackInfo ci) {
        if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "movementInputUpdateEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingImpl;updatePlayerRotations()V"
            ),
            cancellable = true
    )
    private static void updateRotation(CallbackInfo ci) {
        if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
            ci.cancel();
        }
    }
}
