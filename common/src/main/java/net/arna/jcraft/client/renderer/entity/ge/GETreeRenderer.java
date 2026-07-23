package net.arna.jcraft.client.renderer.entity.ge;

import lombok.NonNull;
import net.arna.jcraft.client.renderer.entity.projectiles.ProjectileRenderer;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link ProjectileRenderer} for {@link GETreeEntity}.
 */
public class GETreeRenderer extends ProjectileRenderer<GETreeEntity> {

    public static final String ID = "getree";

    public GETreeRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID),
                b -> b.setShadowRadius(2.5f)
                        .setPrerenderEntry(contextPipeline -> {
                            final var animatable = contextPipeline.animatable();

                            if (animatable.tickCount < 2)
                                contextPipeline.poseStack().scale(0, 0, 0);

                            GETreeEntity.ANIMATION.sendForEntity(animatable);

                            return contextPipeline;
                        }),
                ID);
    }

    @Override
    public boolean shouldRender(
            GETreeEntity entity,
            @NotNull Frustum frustum,
            double x,
            double y,
            double z
    ) {
        return entity.tickCount > 2 && super.shouldRender(entity, frustum, x, y, z);
    }
}
