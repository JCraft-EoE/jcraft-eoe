package net.arna.jcraft.client.renderer.entity.npc;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.TonpettyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The {@link AbstractEntityRenderer} for {@link TonpettyEntity}
 */
@Environment(EnvType.CLIENT)
public class TonpettyRenderer extends SpecUserRenderer<TonpettyEntity> {

    public static final String ID = "tonpetty";

    public TonpettyRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, JCraft.id("geo/hamon_monk.geo.json"), JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID)));
    }

}
