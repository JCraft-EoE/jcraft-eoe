package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.TCBEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class TCBRenderer extends AbstractExoskeletonRenderer<TCBEntity> {

    public TCBRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JStandTypeRegistry.TCB.get());
    }
}
