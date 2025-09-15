package net.shelmarow.betterlockon.mixins;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            ),
            method = {"turnPlayer()V"},
            cancellable = true
    )
    private void epicfight_turnPlayer(CallbackInfo callbackInfo) {
        LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPlayerPatch != null && localPlayerPatch.isTargetLockedOn()) {
            callbackInfo.cancel();
        }

    }
}
