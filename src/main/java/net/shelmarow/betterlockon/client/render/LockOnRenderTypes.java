package net.shelmarow.betterlockon.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class LockOnRenderTypes extends RenderType {

    public LockOnRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = Util.memoize((resourceLocation) -> {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);
        return create("betterlockon:armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });

    public static @NotNull RenderType armorCutoutNoCull(@NotNull ResourceLocation pLocation) {
        return ARMOR_CUTOUT_NO_CULL.apply(pLocation);
    }

    public static RenderType getLockOnQuads(ResourceLocation texture) {
        return RenderType.create(
                "betterlockon:lock_on_texture",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                CompositeState.builder()
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(DepthTestStateShard.NO_DEPTH_TEST)
                        .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                        .setLightmapState(LightmapStateShard.NO_LIGHTMAP)
                        .setOverlayState(OverlayStateShard.NO_OVERLAY)
                        .setCullState(CullStateShard.NO_CULL)
                        .createCompositeState(true)
        );
    }

    public static RenderType getLockOnTriangleFan(ResourceLocation texture) {
        return RenderType.create(
                "betterlockon:lock_on_fan_texture",
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.TRIANGLE_FAN,
                256,
                true,
                false,
                CompositeState.builder()
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(DepthTestStateShard.NO_DEPTH_TEST)
                        .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                        .setLightmapState(LightmapStateShard.NO_LIGHTMAP)
                        .setOverlayState(OverlayStateShard.NO_OVERLAY)
                        .setCullState(CullStateShard.NO_CULL)
                        .createCompositeState(true)
        );
    }

}
