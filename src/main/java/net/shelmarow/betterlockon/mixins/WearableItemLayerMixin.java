package net.shelmarow.betterlockon.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.shelmarow.betterlockon.client.control.LockOnControl;
import net.shelmarow.betterlockon.client.render.LockOnRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = WearableItemLayer.class, remap = false)
public abstract class WearableItemLayerMixin<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends HumanoidModel<E>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, HumanoidArmorLayer<E, M, M>, AM> {

    @Unique private double blo$distance = Double.MAX_VALUE;
    @Unique private boolean blo$canUseAlpha = false;

    public WearableItemLayerMixin(AssetAccessor<AM> mesh) {
        super(mesh);
    }


    @Inject(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "HEAD"
            )
    )
    public void onRender(T entitypatch, E entityliving, HumanoidArmorLayer<E, M, M> vanillaLayer, PoseStack poseStack, MultiBufferSource buf, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks, CallbackInfo ci){
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if(entitypatch.getOriginal() == player && player != null && minecraft.options.getCameraType() != CameraType.FIRST_PERSON){
            blo$canUseAlpha = true;
            blo$distance = minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(player.getEyePosition(partialTicks));
        }
        else {
            blo$canUseAlpha = false;
            blo$distance = Double.MAX_VALUE;
        }
    }

    @Redirect(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/client/renderer/patched/layer/WearableItemLayer;renderArmor(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILyesman/epicfight/api/client/model/SkinnedMesh;Lyesman/epicfight/api/model/Armature;FFFLnet/minecraft/resources/ResourceLocation;[Lyesman/epicfight/api/utils/math/OpenMatrix4f;)V"
            )
    )
    public void renderArmor(WearableItemLayer<E,T,M,AM> instance, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, SkinnedMesh model, Armature armature, float r, float g, float b, ResourceLocation armorTexture, OpenMatrix4f[] poses){
        float alpha = 1;
        if(blo$canUseAlpha){
            alpha =  LockOnControl.getModelAlpha(blo$distance);
        }
        model.draw(poseStack, multiBufferSource, LockOnRenderTypes.armorCutoutNoCull(armorTexture), packedLight, r, g, b, alpha, OverlayTexture.NO_OVERLAY, armature, poses);
    }
}
