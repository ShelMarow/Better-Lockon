package net.shelmarow.betterlockon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nameless.indestructible.main.Indestructible;
import com.nameless.indestructible.world.capability.Utils.IAdvancedCapability;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.shelmarow.betterlockon.BetterLockOn;
import net.shelmarow.betterlockon.client.render.compat.CombatEvolutionCompat;
import net.shelmarow.betterlockon.client.render.icon.IconTypeManager;
import net.shelmarow.betterlockon.client.render.icon.type.IconType;
import net.shelmarow.betterlockon.config.LockOnConfig;
import net.shelmarow.combat_evolution.CombatEvolution;
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
    private static boolean execution = false;
    private static final ResourceLocation EXECUTION_ICON = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/execution.png");

    @Override
    public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> livingEntityPatch, LocalPlayerPatch playerpatch, float partialTicks) {
        LivingEntity target = playerpatch.getTarget();
        if(EpicFightCameraAPI.getInstance().isLockingOnTarget() && entity == target && !entity.isDeadOrDying()){

            healthRatio = target.getHealth()/ target.getMaxHealth();
            staminaRatio = 0F;
            execution = false;

            boolean hasStamina = false;
            //玩家直接显示
            if(livingEntityPatch instanceof PlayerPatch<?> targetPlayer){
                staminaRatio = targetPlayer.getStamina() / targetPlayer.getMaxStamina();
                hasStamina = true;
            }

            //生物先检查CE
            if(ModList.get().isLoaded(CombatEvolution.MOD_ID)){
                if(!hasStamina) {
                    float ratio = CombatEvolutionCompat.isCEPatch(livingEntityPatch);
                    if(ratio >= 0F){
                        staminaRatio = ratio;
                        hasStamina = true;
                    }
                }
                execution = CombatEvolutionCompat.canExecution(livingEntityPatch);
            }

            //检查坚不可摧
            if (!hasStamina && ModList.get().isLoaded(Indestructible.MOD_ID)) {
                if (livingEntityPatch instanceof IAdvancedCapability capability) {
                    staminaRatio = capability.getStamina()/capability.getMaxStamina();
                    hasStamina = true;
                }
            }

            //最后检查史诗战斗的眩晕盾
            if(!hasStamina){
                if (livingEntityPatch != null) {
                    staminaRatio = livingEntityPatch.getStunShield() / livingEntityPatch.getMaxStunShield();
                    hasStamina = true;
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        Matrix4f matrix = getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F,  entity.getBbHeight() * 2 / 3, 0.0F, true, partialTicks);

        float baseSize = (float) LockOnConfig.LOCK_ON_ICON_SIZE.get().doubleValue();
        float size = LockOnConfig.LOCK_ON_SIZE_SCALING.get() ? calculateAdjustedIconSize(baseSize, entity) : baseSize;

        float r = (float) LockOnConfig.LOCK_ON_RED.get().doubleValue();
        float g = (float) LockOnConfig.LOCK_ON_GREEN.get().doubleValue();
        float b = (float) LockOnConfig.LOCK_ON_BLUE.get().doubleValue();
        float a = (float) LockOnConfig.LOCK_ON_ALPHA.get().doubleValue();

        renderLockOn(buffers, matrix, size/2, -size/2, r, g, b, a);
    }

    private float calculateAdjustedIconSize(float baseSize, LivingEntity entity) {
        float entityWidth = entity.getBbWidth();
        float entityHeight = entity.getBbHeight();
        float entitySizeFactor = Math.max(entityWidth, entityHeight);
        final float STANDARD_ENTITY_SIZE = 1f;
        if (entitySizeFactor <= STANDARD_ENTITY_SIZE) {
            return baseSize;
        }

        float scaleFactor = 1.0f + (float) Math.log1p(entitySizeFactor - STANDARD_ENTITY_SIZE) * 0.5f;
        float MAX_SCALE = 2.0f;
        scaleFactor = Math.min(scaleFactor, MAX_SCALE);
        return baseSize * scaleFactor;
    }

    private void renderLockOn(MultiBufferSource buffers, Matrix4f matrix, float max, float min,float r, float g, float b, float alpha) {

        IconType iconType = IconTypeManager.getIconTypeOrDefault(LockOnConfig.LOCK_ON_ICON_TYPES.get());

        //渲染背景
        VertexConsumer vc;
        if(iconType.getBackground() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(iconType.getBackground()));
            vc.vertex(matrix, min, min, 0).uv(0, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max, min, 0).uv(1, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max, max, 0).uv(1, 0).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, min, max, 0).uv(0, 0).color(r, g, b, alpha).endVertex();
        }

        //渲染血量（根据类型内的角度）
        int segments = 100;
        if(iconType.getHealth() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(iconType.getHealth()));
            vc.vertex(matrix, 0, 0, 0).uv(0.5F, 0.5F).color(r, g, b, alpha).endVertex();
            for (int i = 0; i <= segments; i++) {
                float progress = (float) i / segments;
                float angle;
                if (iconType.reverse()) {
                    // 逆时针
                    angle = (float) (iconType.getHealthStartAngle() + healthRatio * iconType.getHealthTotalAngle() * progress);
                } else {
                    // 顺时针
                    angle = (float) (iconType.getHealthStartAngle() - healthRatio * iconType.getHealthTotalAngle() * progress);
                }

                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                float x = cos * max;
                float y = sin * max;
                float u = 0.5F + cos * 0.5F;
                float v = 0.5F - sin * 0.5F;

                vc.vertex(matrix, x, y, 0.0F).uv(u, v).color(r, g, b, alpha).endVertex();
            }
        }

        //渲染耐力（半圆）
        if(iconType.getStamina() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(iconType.getStamina()));
            vc.vertex(matrix, 0, 0, 0).uv(0.5F, 0.5F).color(r, g, b, alpha).endVertex();

            for (int i = 0; i <= segments; i++) {
                float sweep = (float) (Math.PI * staminaRatio);
                float angle = (float) (-Math.PI / 2 - sweep / 2 + sweep * i / segments);
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                float x = cos * max;
                float y = sin * max;
                float u = 0.5F + cos * 0.5F;
                float v = 0.5F - sin * 0.5F;

                vc.vertex(matrix, x, y, 0.0F).uv(u, v).color(r, g, b, alpha).endVertex();
            }
        }

        //渲染遮罩
        if(iconType.getOverlay() != null) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(iconType.getOverlay()));
            vc.vertex(matrix, min, min, 0).uv(0, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max, min, 0).uv(1, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max, max, 0).uv(1, 0).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, min, max, 0).uv(0, 0).color(r, g, b, alpha).endVertex();
        }

        //渲染处决图标
        if(execution) {
            vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(EXECUTION_ICON));
            float scale = 1.5F;
            vc.vertex(matrix, min * scale, min * scale, 0).uv(0, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max * scale, min * scale, 0).uv(1, 1).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, max * scale, max * scale, 0).uv(1, 0).color(r, g, b, alpha).endVertex();
            vc.vertex(matrix, min * scale, max * scale, 0).uv(0, 0).color(r, g, b, alpha).endVertex();
        }
    }
}
