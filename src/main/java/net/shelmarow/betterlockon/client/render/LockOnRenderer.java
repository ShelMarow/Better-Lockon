package net.shelmarow.betterlockon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shelmarow.betterlockon.BetterLockOn;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@OnlyIn(Dist.CLIENT)
public class LockOnRenderer extends EntityUI {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/lock_on_background.png");
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/lock_on_overlay.png");
    private static final ResourceLocation RING = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/lock_on_ring.png");
    private static final ResourceLocation STAMINA = ResourceLocation.fromNamespaceAndPath(BetterLockOn.MOD_ID, "textures/hud/lock_on_stamina.png");

    private static float healthRatio = 1F;
    private static float staminaRatio = 0F;

    @Override
    public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> livingEntityPatch, LocalPlayerPatch playerpatch, float v) {
        LivingEntity target = playerpatch.getTarget();
        if(playerpatch.isTargetLockedOn() && entity == target && !entity.isDeadOrDying()){
            healthRatio = target.getHealth()/ target.getMaxHealth();
            staminaRatio = 0F;
            if(livingEntityPatch instanceof PlayerPatch<?> targetPlayer){
                staminaRatio = targetPlayer.getStamina() / targetPlayer.getMaxStamina();
            }
            return true;
        }
        return false;
    }

    @Override
    public void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
        Matrix4f matrix = getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F,  entity.getBbHeight() * 2 / 3, 0.0F, true, partialTicks);

        // 从配置获取基础大小
        float baseSize = (float) LockOnConfig.LOCK_ON_ICON_SIZE.get().doubleValue();

        // 根据实体大小智能调整尺寸（如果启用）
        float size = LockOnConfig.LOCK_ON_SIZE_SCALING.get() ? calculateAdjustedIconSize(baseSize, entity) : baseSize;

        // 从配置获取颜色值
        float r = (float) LockOnConfig.LOCK_ON_RED.get().doubleValue();
        float g = (float) LockOnConfig.LOCK_ON_GREEN.get().doubleValue();
        float b = (float) LockOnConfig.LOCK_ON_BLUE.get().doubleValue();
        float a = (float) LockOnConfig.LOCK_ON_ALPHA.get().doubleValue();

        renderLockOn(
                buffers, matrix,
                size/2, -size/2,
                r, g, b, a
        );
    }

    /**
     * 根据实体大小智能调整图标尺寸
     * 只对大型实体增大图标，小型实体保持基础大小
     */
    private float calculateAdjustedIconSize(float baseSize, LivingEntity entity) {
        // 获取实体的边界框尺寸
        float entityWidth = entity.getBbWidth();
        float entityHeight = entity.getBbHeight();

        // 计算实体体积因子（使用最大尺寸）
        float entitySizeFactor = Math.max(entityWidth, entityHeight);

        // 标准实体尺寸参考（玩家约为0.6宽，1.8高）
        final float STANDARD_ENTITY_SIZE = 1f;

        // 只对大于标准尺寸的实体进行放大
        if (entitySizeFactor <= STANDARD_ENTITY_SIZE) {
            return baseSize; // 小型实体保持原大小
        }

        // 计算放大比例（使用对数缩放避免过大）
        float scaleFactor = 1.0f + (float) Math.log1p(entitySizeFactor - STANDARD_ENTITY_SIZE) * 0.5f;

        // 限制最大放大倍数（避免图标过大）
        final float MAX_SCALE = 3.0f;
        scaleFactor = Math.min(scaleFactor, MAX_SCALE);

        return baseSize * scaleFactor;
    }

    private void renderLockOn(MultiBufferSource buffers, Matrix4f matrix, float max, float min,
                              float r, float g, float b, float alpha) {

        //渲染背景
        VertexConsumer vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(LockOnRenderer.BACKGROUND));
        vc.vertex(matrix, min, min, 0).uv(0, 1).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, max, min, 0).uv(1, 1).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, max, max, 0).uv(1, 0).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, min, max, 0).uv(0, 0).color(r, g, b, alpha).endVertex();

        //渲染血量（圆形）
        int segments = 100;
        vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(LockOnRenderer.RING));
        vc.vertex(matrix, 0, 0, 0).uv(0.5F, 0.5F).color(r, g, b, alpha).endVertex();
        for (int i = 0; i <= segments; i++) {
            float angle = (float) ((Math.PI / 2) - (healthRatio * 2 * Math.PI) * i / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float x = cos * max;
            float y = sin * max;
            float u = 0.5F + cos * 0.5F;
            float v = 0.5F - sin * 0.5F;

            vc.vertex(matrix, x, y, 0.0F).uv(u, v).color(r, g, b, alpha).endVertex();
        }

        //渲染耐力（半圆）
        vc = buffers.getBuffer(LockOnRenderTypes.getLockOnTriangleFan(LockOnRenderer.STAMINA));
        vc.vertex(matrix, 0, 0, 0).uv(0.5F,0.5F).color(r, g, b, alpha).endVertex();

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

        //渲染遮罩
        vc = buffers.getBuffer(LockOnRenderTypes.getLockOnQuads(LockOnRenderer.OVERLAY));
        vc.vertex(matrix, min, min, 0).uv(0, 1).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, max, min, 0).uv(1, 1).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, max, max, 0).uv(1, 0).color(r, g, b, alpha).endVertex();
        vc.vertex(matrix, min, max, 0).uv(0, 0).color(r, g, b, alpha).endVertex();
    }
}
