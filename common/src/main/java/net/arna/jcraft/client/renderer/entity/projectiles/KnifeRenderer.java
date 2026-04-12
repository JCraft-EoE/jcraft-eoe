package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

/**
 * The {@link ProjectileRenderer} for {@link KnifeProjectile}.
 */
@Environment(EnvType.CLIENT)
public class KnifeRenderer extends ProjectileRenderer<KnifeProjectile> {

    public static final String ID = "knife";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public KnifeRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE),
                ID);
    }

    @Override
    public int getBlockLightLevel(final @NonNull KnifeProjectile entityIn, final @NonNull BlockPos blockPos) {
        return (entityIn.getLightning() || entityIn.isOnFire()) ? 15 : entityIn.level().getBrightness(LightLayer.BLOCK, entityIn.blockPosition());
    }

}
