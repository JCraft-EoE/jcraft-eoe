package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;

/**
 * The {@link ProjectileRenderer} for {@link AnkhProjectile}.
 */
@Environment(EnvType.CLIENT)
public class AnkhRenderer extends ProjectileRenderer<AnkhProjectile> {

    public static final String ID = "ankh";
    private static final RenderType RENDER_TYPE = RenderType.eyes(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public AnkhRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE),
                ID);
    }

    @Override
    public int getBlockLightLevel(final @NonNull AnkhProjectile entity, final @NonNull BlockPos pos) {
        return 15;
    }

}
