package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * The {@link ProjectileRenderer} for {@link BubbleProjectile}.
 */
@Environment(EnvType.CLIENT)
public class BubbleRenderer extends ProjectileRenderer<BubbleProjectile> {

    public static final String ID = "bubble";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public BubbleRenderer(@NonNull final EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE)
                        .setPrerenderEntry((pc) -> {
                            pc.setAlpha( // third root ( distance squared )
                                    Mth.fastInvCubeRoot((float) JUtils.nullSafeDistanceSqr(Minecraft.getInstance().player, pc.animatable()))
                            );

                            return pc;
                        }),
                ID);
    }
}
