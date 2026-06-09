package net.shelmarow.betterlockon.compat.mixins;

import com.github.leawind.thirdperson.core.EntityAgent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = EntityAgent.class,remap = false)
public class EntityAgentMixin {
    @Inject(
            method = "onRenderTickStart",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelRotation1(CallbackInfo ci){
        LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getLocalPlayerPatch(Minecraft.getInstance().player);
        if (localPlayerPatch != null) {
            if (localPlayerPatch.getTarget() != null && EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
                ci.cancel();
            }
            if(localPlayerPatch.getEntityState().getLevel() != 0 && !localPlayerPatch.getEntityState().turningLocked()){
                localPlayerPatch.getOriginal().yBodyRot = localPlayerPatch.getOriginal().getYRot();
                localPlayerPatch.setModelYRot(localPlayerPatch.getOriginal().getYRot(),true);
            }
        }
    }
    @Inject(
            method = "onClientTickStart",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelRotation2(CallbackInfo ci){
        LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(Minecraft.getInstance().player, LocalPlayerPatch.class);
        if (localPlayerPatch != null) {
            if (localPlayerPatch.getTarget() != null && EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
                ci.cancel();
            }
        }
    }
}
