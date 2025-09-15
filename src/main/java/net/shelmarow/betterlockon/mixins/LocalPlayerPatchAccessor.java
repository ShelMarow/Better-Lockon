package net.shelmarow.betterlockon.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = LocalPlayerPatch.class, remap = false)
public interface LocalPlayerPatchAccessor {
    @Accessor("rayTarget")
    void setRayTarget(LivingEntity target);

    @Accessor("lockOnXRot")
    void setLockOnXRot(float x);
    @Accessor("lockOnXRotO")
    void setLockOnXRotO(float x);

    @Accessor("lockOnYRot")
    void setLockOnYRot(float y);
    @Accessor("lockOnYRotO")
    void setLockOnYRotO(float y);
}
