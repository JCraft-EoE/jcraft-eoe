package net.arna.jcraft.client.renderer.entity.ge;

import lombok.NonNull;
import net.arna.jcraft.client.renderer.entity.projectiles.ProjectileRenderer;
import net.arna.jcraft.common.entity.projectile.GEVinesEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link ProjectileRenderer} for {@link GEVinesEntity}.
 */
public class GEVinesRenderer extends ProjectileRenderer<GEVinesEntity> {

    public static final String ID = "gevine";

    public GEVinesRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID),
                b -> b.setShadowRadius(1.5f)
                        .setPrerenderEntry(contextPipeline -> {
                            final var animatable = contextPipeline.animatable();

                            if (animatable.tickCount < 2)
                                contextPipeline.poseStack().scale(0, 0, 0);

                            GEVinesEntity.ANIMATION.sendForEntity(animatable);

                            return contextPipeline;
                        }),
                ID);
    }
}
