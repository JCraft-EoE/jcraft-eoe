package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.SandTornadoEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link SandTornadoEntity}.
 */
@Environment(EnvType.CLIENT)
public class SandTornadoRenderer extends AbstractEntityRenderer<SandTornadoEntity> {

    public static final String ID = "sandtornado";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public SandTornadoRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b.setShadowRadius(1.1f).setRenderType(RENDER_TYPE), ID);
    }

    @Override
    public boolean shouldShowName(final @NonNull SandTornadoEntity animatable) {
        return false;
    }
}
