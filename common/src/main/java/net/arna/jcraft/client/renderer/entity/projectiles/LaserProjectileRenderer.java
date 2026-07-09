package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.LaserProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link ProjectileRenderer} for {@link LaserProjectile}.
 */
@Environment(EnvType.CLIENT)
public class LaserProjectileRenderer extends ProjectileRenderer<LaserProjectile> {

    public static final String ID = "laser";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public LaserProjectileRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE),
                ID);
    }

}
