package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link ProjectileRenderer} for {@link RedBindEntity}.
 */
public class RedBindRenderer extends AbstractEntityRenderer<RedBindEntity> {

    public static final String ID = "red_bind";
    private static final RenderType RENDER_TYPE = RenderType.eyes(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public RedBindRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                        .setPrerenderEntry((pc) -> {
                            final var animatable = pc.animatable();
                            final var poseStack = pc.poseStack();

                            float xz = animatable.getBoundWidth();
                            poseStack.scale(xz, 1f, xz);

                            return pc;
                        })
                .setRenderType(RENDER_TYPE),
                ID);
    }

    @Override
    public boolean shouldShowName(@NotNull RedBindEntity entity) {
        return false;
    }

    @Override
    public int getBlockLightLevel(final @NonNull RedBindEntity entity, final @NonNull BlockPos pos) {
        return 15;
    }
}
