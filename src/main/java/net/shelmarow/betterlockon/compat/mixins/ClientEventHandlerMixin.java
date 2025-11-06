package net.shelmarow.betterlockon.compat.mixins;

import com.github.exopandora.shouldersurfing.forge.event.ClientEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = ClientEventHandler.class,remap = false)
public class ClientEventHandlerMixin {
    @Inject(
            method = "movementInputUpdateEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/exopandora/shouldersurfing/client/InputHandler;updateMovementInput(Lnet/minecraft/client/player/Input;)V"
            ),
            cancellable = true
    )
    private static void movementInputUpdateEvent(CallbackInfo ci){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isTargetLockedOn()) {
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
    private static void updateRotation(CallbackInfo ci){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isTargetLockedOn()) {
            ci.cancel();
        }
    }
}
