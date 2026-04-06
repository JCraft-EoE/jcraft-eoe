package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.MandomEntity;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class TuskAct2Renderer extends StandEntityRenderer<TuskAct2Entity> {

    public TuskAct2Renderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.setRenderType(renderType(RenderType::entityTranslucentCull)), JStandTypeRegistry.TUSK_ACT_2.get(), 0f, 0f);
    }

}