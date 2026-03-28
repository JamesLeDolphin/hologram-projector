package com.jdolphin.holoprojector.client;

import java.awt.Color;
import java.util.Map;

import com.jdolphin.holoprojector.common.block.HoloProjectorBlock;
import com.jdolphin.holoprojector.common.block.HoloProjectorBlockEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HoloProjectorRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity p_112138_) {
        return true;
    }

    public int getViewDistance() {
        return 256;
    }

    public boolean shouldRender(HoloProjectorBlockEntity blockEntity, Vec3 vec3) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(vec3.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

    private static RenderType depthRenderType(ResourceLocation texture) {
        return RenderType.create(
            "holoprojector:depth",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .setCullState(RenderStateShard.CULL)
                .setWriteMaskState(RenderStateShard.DEPTH_WRITE)
                .createCompositeState(false)
        );
    }

    @Override
    public void render(HoloProjectorBlockEntity be, float delta, PoseStack stack, MultiBufferSource source, int light, int overlay) {
        if (be.getBlockState().getValue(HoloProjectorBlock.POWERED)) {
            GameProfile profile = be.getTargetPlayer();
            stack.pushPose();
            stack.translate(0.5, 1.6, 0.5);
            stack.mulPose(Axis.ZN.rotationDegrees(180));
            stack.mulPose(Axis.YP.rotationDegrees(be.getBlockState().getValue(HoloProjectorBlock.FACING).getOpposite().toYRot()));
            if (!be.isSolid()) stack.scale(1, 1 - ((float) Math.random()) * 0.02f, 1);

            PlayerModel<LivingEntity> model = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(be.isSlim() ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), be.isSlim());
            model.young = false;
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            ResourceLocation skinTexture = null;

            if (profile != null) {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getInstance().getMinecraftSessionService().getTextures(profile, false);
                MinecraftProfileTexture texture = map.get(MinecraftProfileTexture.Type.SKIN);
                if (texture != null) {
                    skinTexture = skinManager.registerTexture(texture, MinecraftProfileTexture.Type.SKIN);
                }
            }
            if (skinTexture == null) skinTexture = DefaultPlayerSkin.getDefaultSkin();

            if (be.isSolid()) {
                VertexConsumer consumer = source.getBuffer(RenderType.entityCutout(skinTexture));
            	model.renderToBuffer(stack, consumer, LightTexture.FULL_BRIGHT, overlay, 1, 1, 1, 1);
            } else {
                VertexConsumer depthConsumer = source.getBuffer(depthRenderType(skinTexture));
                model.renderToBuffer(stack, depthConsumer, LightTexture.FULL_BRIGHT, overlay, 1, 1, 1, 1);

                Color color = new Color(be.getColor());

                float r = color.getRed() / 255f;
                float g = color.getGreen() / 255f;
                float b = color.getBlue() / 255f;

                VertexConsumer colorConsumer = source.getBuffer(RenderType.entityTranslucent(skinTexture));
                model.renderToBuffer(stack, colorConsumer, LightTexture.FULL_BRIGHT, overlay, r, g, b, 0.8f);
            }
            stack.popPose();
        }
    }
}
