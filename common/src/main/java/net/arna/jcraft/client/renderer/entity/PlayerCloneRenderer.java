package net.arna.jcraft.client.renderer.entity;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PlayerCloneRenderer extends HumanoidMobRenderer<PlayerCloneEntity, HumanoidModel<PlayerCloneEntity>> {
    private final PlayerRenderer parent;

    public PlayerCloneRenderer(final EntityRendererProvider.Context ctx, final boolean slim) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5f);
        parent = new PlayerRenderer(ctx, slim);
    }

    @Override
    public boolean shouldRender(final PlayerCloneEntity clone, final Frustum frustum, final double d, final double e, final double f) {
        final boolean s = super.shouldRender(clone, frustum, d, e, f);

        if (clone.shouldRenderForMaster()) {
            return s;
        }

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            final UUID masterId = clone.getMasterId();
            if (masterId == null) {
                return s;
            }
            return !masterId.equals(player.getUUID());
        }
        return s;
    }

    @Override
    public void render(final PlayerCloneEntity clone, final float f, final float g, final PoseStack matrixStack, final MultiBufferSource vertexConsumerProvider, final int i) {
        final PlayerCloneClientPlayerEntity clonePlayer = CloneSkinTracker.toPlayer(clone);
        if (clonePlayer == null) {
            return;
        }
        parent.render(clonePlayer, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public ResourceLocation getTextureLocation(final PlayerCloneEntity entity) {
        return CloneSkinTracker.getSkinFor(entity, MinecraftProfileTexture.Type.SKIN);
    }
}

