package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link SheerHeartAttackEntity}.
 */
@Environment(EnvType.CLIENT)
public class SheerHeartAttackRenderer extends AbstractEntityRenderer<SheerHeartAttackEntity> {

    public static final String ID = "sha";
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));

    public SheerHeartAttackRenderer(final EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), b -> b
                .setRenderType(RENDER_TYPE),
                ID);
    }

}