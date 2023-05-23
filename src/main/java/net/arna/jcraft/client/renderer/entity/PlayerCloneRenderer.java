package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.entity.PlayerCloneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class PlayerCloneRenderer extends BipedEntityRenderer<PlayerCloneEntity, BipedEntityModel<PlayerCloneEntity>> {
    protected final static Identifier TEXTURE = new Identifier("textures/entity/steve.png");

    public PlayerCloneRenderer(EntityRendererFactory.Context ctx, boolean slim) {
        super(ctx, new PlayerEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        this.addFeature(
                new ArmorFeatureRenderer(this,
                    new BipedEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_INNER_ARMOR : EntityModelLayers.PLAYER_INNER_ARMOR))
                    , new BipedEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR))
                )
        );
    }

    @Override
    public void render(PlayerCloneEntity mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F); // Player scale
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
        matrixStack.pop();
    }


    @Override
    public Identifier getTexture(PlayerCloneEntity mobEntity) {
        String ownerName = mobEntity.getOwnerName();
        if (!Objects.equals(ownerName, "%unset_owner_name")) {
            PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(String.valueOf(ownerName));
            if (playerListEntry != null) {
                // Not null handling done inside function
                return playerListEntry.getSkinTexture();
            }
        }

        return TEXTURE;
    }
}

