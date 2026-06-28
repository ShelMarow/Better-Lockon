package net.shelmarow.betterlockon.mixins;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(LocalPlayerPatch.class)
public abstract class LocalPlayerPatchMixin extends AbstractClientPlayerPatch<LocalPlayer> {

    @Inject(
            method = "toVanillaMode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V")
    )
    private void toVanillaMode(boolean synchronize, CallbackInfo ci) {
        EpicFightCameraAPI cameraAPI = EpicFightCameraAPI.getInstance();
        original.setXRot(cameraAPI.getCameraXRot());
        original.setYRot(cameraAPI.getCameraYRot());
    }
}
