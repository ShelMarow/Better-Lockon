package net.shelmarow.betterlockon.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class LockOnRenderTypes extends RenderType {

    public LockOnRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static RenderType getLockOnQuads(ResourceLocation texture) {
        return RenderType.create(
                "efn:lock_on_texture",
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
                "efn:lock_on_fan_texture",
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
