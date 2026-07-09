package net.arna.jcraft.client.renderer.entity.npc;

import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.PetshopEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link PetshopEntity}
 */
public class PetshopRenderer extends AbstractEntityRenderer<PetshopEntity> {

    public static String ID = "petshop";

    public PetshopRenderer(final EntityRendererProvider.Context renderManager) {
        super(renderManager, () -> new EntityAnimator<>(ID), ID);
    }
}
