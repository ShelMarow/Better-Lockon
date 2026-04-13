package net.shelmarow.betterlockon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.shelmarow.betterlockon.client.render.icon.IconTypeManager;
import net.shelmarow.betterlockon.client.render.icon.type.IconType;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@OnlyIn(Dist.CLIENT)
public class LockOnRenderer extends EntityUI {
    private static float healthRatio = 1F;
    private static float staminaRatio = 0F;

    @Override
    public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> livingEntityPatch, LocalPlayerPatch playerpatch, float partialTicks) {
        LivingEntity target = playerpatch.getTarget();
        if (EpicFightCameraAPI.getInstance().isLockingOnTarget() && entity == target && !entity.isDeadOrDying()) {
            healthRatio = target.getHealth() / target.getMaxHealth();
            staminaRatio = 0F;

            boolean hasStamina = false;
            if (livingEntityPatch instanceof PlayerPatch<?> targetPlayer) {
                staminaRatio = targetPlayer.getStamina() / targetPlayer.getMaxStamina();
                hasStamina = true;
            }

            if (!hasStamina && ModList.get().isLoaded(Indestructible.MOD_ID) && livingEntityPatch instanceof IAdvancedCapability capability) {
                staminaRatio = capability.getStamina() / capability.getMaxStamina();
                hasStamina = true;
            }

            if (!hasStamina && livingEntityPatch != null) {
                staminaRatio = livingEntityPatch.getStunShield() / livingEntityPatch.getMaxStunShield();
            }

            return true;
        }
        return false;
    }

    @Override
    public void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        poseStack.pushPose();
        EntityUI.setupPoseStack(poseStack, entity, 0.0F, entity.getBbHeight() * 2 / 3, 0.0F, true, partialTicks);
        Matrix4f matrix = poseStack.last().pose();

        float baseSize = (float) LockOnConfig.LOCK_ON_ICON_SIZE.get().doubleValue();
        float size = LockOnConfig.LOCK_ON_SIZE_SCALING.get() ? calculateAdjustedIconSize(baseSize, entity) : baseSize;

        float r = (float) LockOnConfig.LOCK_ON_RED.get().doubleValue();
        float g = (float) LockOnConfig.LOCK_ON_GREEN.get().doubleValue();
        float b = (float) LockOnConfig.LOCK_ON_BLUE.get().doubleValue();
        float a = (float) LockOnConfig.LOCK_ON_ALPHA.get().doubleValue();

        renderLockOn(buffers, matrix, size / 2, -size / 2, r, g, b, a);
        poseStack.popPose();
    }

    private float calculateAdjustedIconSize(float baseSize, LivingEntity entity) {
        float entitySizeFactor = Math.max(entity.getBbWidth(), entity.getBbHeight());
        final float standardEntitySize = 1F;

        if (entitySizeFactor <= standardEntitySize) {
            return baseSize;
        }

        float scaleFactor = 1.0f + (float) Math.log1p(entitySizeFactor - standardEntitySize) * 0.5f;
        return baseSize * Math.min(scaleFactor, 2.0F);
    }

    private void renderLockOn(MultiBufferSource buffers, Matrix4f matrix, float max, float min, float r, float g, float b, float alpha) {
        IconType iconType = IconTypeManager.getIconTypeOrDefault(LockOnConfig.LOCK_ON_ICON_TYPES.get());
        VertexConsumer vc;

        if (iconType.getBackground() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(iconType.getBackground()));
            vc.addVertex(matrix, min, min, 0).setUv(0, 1).setColor(r, g, b, alpha);
            vc.addVertex(matrix, max, min, 0).setUv(1, 1).setColor(r, g, b, alpha);
            vc.addVertex(matrix, max, max, 0).setUv(1, 0).setColor(r, g, b, alpha);
            vc.addVertex(matrix, min, max, 0).setUv(0, 0).setColor(r, g, b, alpha);
        }

        int segments = 100;
        if (iconType.getHealth() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(iconType.getHealth()));
            vc.addVertex(matrix, 0, 0, 0).setUv(0.5F, 0.5F).setColor(r, g, b, alpha);
            for (int i = 0; i <= segments; i++) {
                float progress = (float) i / segments;
                float angle = iconType.reverse()
                        ? (float) (iconType.getHealthStartAngle() + healthRatio * iconType.getHealthTotalAngle() * progress)
                        : (float) (iconType.getHealthStartAngle() - healthRatio * iconType.getHealthTotalAngle() * progress);

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                vc.addVertex(matrix, cos * max, sin * max, 0.0F)
                        .setUv(0.5F + cos * 0.5F, 0.5F - sin * 0.5F)
                        .setColor(r, g, b, alpha);
            }
        }

        if (iconType.getStamina() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(iconType.getStamina()));
            vc.addVertex(matrix, 0, 0, 0).setUv(0.5F, 0.5F).setColor(r, g, b, alpha);

            for (int i = 0; i <= segments; i++) {
                float sweep = (float) (Math.PI * staminaRatio);
                float angle = (float) (-Math.PI / 2 - sweep / 2 + sweep * i / segments);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                vc.addVertex(matrix, cos * max, sin * max, 0.0F)
                        .setUv(0.5F + cos * 0.5F, 0.5F - sin * 0.5F)
                        .setColor(r, g, b, alpha);
            }
        }

        if (iconType.getOverlay() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(iconType.getOverlay()));
            vc.addVertex(matrix, min, min, 0).setUv(0, 1).setColor(r, g, b, alpha);
            vc.addVertex(matrix, max, min, 0).setUv(1, 1).setColor(r, g, b, alpha);
            vc.addVertex(matrix, max, max, 0).setUv(1, 0).setColor(r, g, b, alpha);
            vc.addVertex(matrix, min, max, 0).setUv(0, 0).setColor(r, g, b, alpha);
        }
    }
}
