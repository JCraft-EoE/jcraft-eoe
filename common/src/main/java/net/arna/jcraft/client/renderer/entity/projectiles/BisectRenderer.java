package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.common.entity.projectile.BisectProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BisectRenderer extends ProjectileRenderer<BisectProjectile> {
    public static final String ID = "bisect";

    public BisectRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, BisectAnimator::new, ID);
    }

    public static class BisectAnimator extends EntityAnimator<BisectProjectile> {

        public BisectAnimator() {
            super(ID);
        }

        @Override
        public void setCustomAnimations(final @NonNull BisectProjectile animatable, final float partialTicks) {
            super.setCustomAnimations(animatable, partialTicks);
            context().boneCache().getBakedModel().getBone("base").ifPresent(base -> {
                final float scale = animatable.getScale();
                base.setScaleX(scale);
                if (scale < 0.5f) {
                    base.setScaleZ(scale);
                }
            });
        }
    }

}
