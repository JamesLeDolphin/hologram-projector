package com.jdolphin.holoprojector.client;

import com.jdolphin.holoprojector.common.block.HoloProjectorBlock;
import com.jdolphin.holoprojector.common.block.HoloProjectorBlockEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.awt.Color;
import java.util.Map;
import java.util.Optional;

public class HoloProjectorRenderer implements BlockEntityRenderer<HoloProjectorBlockEntity> {

    public boolean shouldRenderOffScreen(HoloProjectorBlockEntity entity) {
        return true;
    }

    public boolean shouldRender(HoloProjectorBlockEntity entity, Vec3 vec3) {
        return true;
    }

    @Override
    public void render(HoloProjectorBlockEntity be, float delta, PoseStack stack, MultiBufferSource source, int light, int overlay) {
        GameProfile profile = be.getTargetPlayer();
        stack.translate(0.5, 1.6, 0.5);
        stack.mulPose(Axis.ZN.rotationDegrees(180));
        stack.mulPose(Axis.YP.rotationDegrees(be.getBlockState().getValue(HoloProjectorBlock.FACING).getOpposite().toYRot()));
        if (!be.isSolid()) stack.scale(1, 1 - ((float) Math.random()) * 0.02f, 1);

        PlayerModel<LivingEntity> model = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(be.isSlim() ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), be.isSlim());
        model.young = false;
        SkinManager skinManager = Minecraft.getInstance().getSkinManager();
        RenderType type = null;

        if (profile != null) {
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getInstance().getMinecraftSessionService().getTextures(profile, false);
            MinecraftProfileTexture texture = map.get(MinecraftProfileTexture.Type.SKIN);
            if (texture != null) {
                type = RenderType.entityTranslucent(skinManager.registerTexture(texture, MinecraftProfileTexture.Type.SKIN));
            }
        }
        if (type == null) type = RenderType.entityTranslucent(DefaultPlayerSkin.getDefaultSkin());

        VertexConsumer consumer = source.getBuffer(type);
        Color color = new Color(be.getColor(), true);
        float r = color.getRed() / 255;
        float g = color.getGreen() / 255;
        float b = color.getBlue() / 255;
        float a = color.getAlpha() / 255;
        if (be.isSolid()) model.renderToBuffer(stack, consumer, LightTexture.FULL_BRIGHT, overlay, 1, 1, 1, 1);
        else model.renderToBuffer(stack, consumer, LightTexture.FULL_BRIGHT, overlay, r, g, b, a);
    }
}
