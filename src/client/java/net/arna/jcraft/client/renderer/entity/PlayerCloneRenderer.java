package net.arna.jcraft.client.renderer.entity;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class PlayerCloneRenderer extends BipedEntityRenderer<PlayerCloneEntity, BipedEntityModel<PlayerCloneEntity>> {
    private final PlayerEntityRenderer parent;

    public PlayerCloneRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        parent = new PlayerEntityRenderer(ctx, slim);
    }

    @Override
    public boolean shouldRender(PlayerCloneEntity clone, Frustum frustum, double d, double e, double f) {
        boolean s = super.shouldRender(clone, frustum, d, e, f);

        if (clone.shouldRenderForMaster()) return s;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            UUID masterId = clone.getMasterId();
            if (masterId == null) return s;
            return !masterId.equals(player.getUuid());
        }
        return s;
    }

    @Override
    public void render(PlayerCloneEntity clone, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        PlayerCloneClientPlayerEntity clonePlayer = CloneSkinTracker.toPlayer(clone);
        if (clonePlayer == null) return;
        parent.render(clonePlayer, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(PlayerCloneEntity entity) {
        return CloneSkinTracker.getSkinFor(entity, MinecraftProfileTexture.Type.SKIN);
    }
}

