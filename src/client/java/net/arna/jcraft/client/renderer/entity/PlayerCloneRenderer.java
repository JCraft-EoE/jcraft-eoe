package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class PlayerCloneRenderer extends BipedEntityRenderer<PlayerCloneEntity, BipedEntityModel<PlayerCloneEntity>> {
    private final PlayerEntityRenderer parent;

    public PlayerCloneRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        parent = new PlayerEntityRenderer(ctx, slim);
    }

    @Override
    public void render(PlayerCloneEntity clone, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (JClientUtils.shouldNotRenderClone(clone)) return;

        parent.render(CloneSkinTracker.toPlayer(clone), f, g, matrixStack, vertexConsumerProvider, i);
    }
}

