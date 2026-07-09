package net.arna.jcraft.client.renderer.entity.npc;

import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.DarbyYoungerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link DarbyYoungerEntity}
 */
@Environment(EnvType.CLIENT)
public class DarbyYoungerRenderer extends AbstractEntityRenderer<DarbyYoungerEntity> {

    public static final String ID = "darby_younger";

    public DarbyYoungerRenderer(final EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(ID), ID);
    }
}
