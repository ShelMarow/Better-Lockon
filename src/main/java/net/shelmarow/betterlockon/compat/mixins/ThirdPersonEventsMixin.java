package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.ThirdPersonEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = ThirdPersonEvents.class,remap = false)
public class ThirdPersonEventsMixin {
    @Inject(
            method = "onCalculateMoveImpulse",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void onMoveImpulse(CallbackInfo ci){
        LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch != null && playerPatch.isTargetLockedOn()) {
            ci.cancel();
        }
    }
}
