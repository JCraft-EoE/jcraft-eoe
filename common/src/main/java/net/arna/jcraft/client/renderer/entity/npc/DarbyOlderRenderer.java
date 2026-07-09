package net.arna.jcraft.client.renderer.entity.npc;

import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.DarbyOlderEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link DarbyOlderEntity}
 */
@Environment(EnvType.CLIENT)
public class DarbyOlderRenderer extends AbstractEntityRenderer<DarbyOlderEntity> {

    public static final String ID = "darby_older";

    public DarbyOlderRenderer(final EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), ID);
    }
}
