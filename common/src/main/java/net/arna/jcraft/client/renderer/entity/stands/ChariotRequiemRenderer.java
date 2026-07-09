package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.layer.SCROutlineLayer;
import net.arna.jcraft.common.entity.stand.ChariotRequiemEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link StandEntityRenderer} for {@link ChariotRequiemEntity}
 */
@Environment(EnvType.CLIENT)
public class ChariotRequiemRenderer extends StandEntityRenderer<ChariotRequiemEntity> {
    public ChariotRequiemRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.addRenderLayer(new SCROutlineLayer()), JStandTypeRegistry.CHARIOT_REQUIEM.get(), 0f, 0f);
    }
}
