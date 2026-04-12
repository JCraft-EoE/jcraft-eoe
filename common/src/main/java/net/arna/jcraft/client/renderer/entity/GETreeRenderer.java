package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import net.arna.jcraft.client.renderer.entity.projectiles.ProjectileRenderer;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link ProjectileRenderer} for {@link GETreeEntity}.
 */
public class GETreeRenderer extends ProjectileRenderer<GETreeEntity> {

    public static final String ID = "getree";

    public GETreeRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID),
                b -> b.setShadowRadius(2.5f)
                        .setRenderEntry(contextPipeline -> {
                            final var animatable = contextPipeline.animatable();

                            GETreeEntity.ANIMATION.sendForEntity(animatable);

                            return contextPipeline;
                        }),
                ID);
    }
}
