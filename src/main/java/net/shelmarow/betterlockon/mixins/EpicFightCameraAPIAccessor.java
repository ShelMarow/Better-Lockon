package net.shelmarow.betterlockon.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

import javax.annotation.Nullable;

@Mixin(value = EpicFightCameraAPI.class, remap = false)
public interface EpicFightCameraAPIAccessor {

    @Accessor("focusingEntity")
    void setFocusingEntity(@Nullable LivingEntity entity);

    @Accessor("cameraYRot")
    float getCameraYRot();
    @Accessor("cameraYRot")
    void setCameraYRot(float yRot);

    @Accessor("cameraYRotO")
    float getCameraYRotO();
    @Accessor("cameraYRotO")
    void setCameraYRotO(float yRot);

    @Accessor("cameraXRot")
    float getCameraXRot();
    @Accessor("cameraXRot")
    void setCameraXRot(float xRot);

    @Accessor("cameraXRotO")
    float getCameraXRotO();
    @Accessor("cameraXRotO")
    void setCameraXRotO(float xRot);
}
