package net.arna.jcraft.client.renderer.entity.projectiles;

import net.arna.jcraft.common.entity.projectile.FireSparkProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple renderer for {@link FireSparkProjectile} - uses particle effects instead of models.
 */
public class FireSparkRenderer extends EntityRenderer<FireSparkProjectile> {

    public FireSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FireSparkProjectile entity) {
        // Return empty texture since we're using particles for visual effects
        return new ResourceLocation("minecraft", "textures/particle/flame.png");
    }
}