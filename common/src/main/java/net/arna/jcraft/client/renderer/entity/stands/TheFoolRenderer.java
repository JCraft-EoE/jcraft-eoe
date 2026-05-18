package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TheFoolRenderer extends StandEntityRenderer<TheFoolEntity> {
    public static final ResourceLocation MODEL = JCraft.id("geo/the_fool.geo.json");
    public static final ResourceLocation SAND_TEXTURE = JCraft.id("textures/entity/stands/the_fool/sand.png");

    public TheFoolRenderer(@NotNull EntityRendererProvider.Context context) {
        super(context, b -> b.setRenderType(TheFoolRenderer::renderType),
                entity -> MODEL, TheFoolRenderer::getTexture, JStandTypeRegistry.THE_FOOL.get(),
                false, false,0.7854f, -0.349f, 30f);
    }

    private static @NotNull RenderType renderType(final @NonNull TheFoolEntity fool) {
        return renderTypeOf(fool, getTexture(fool));
    }

    private static ResourceLocation getTexture(TheFoolEntity fool) {
        return fool.isSand() ? SAND_TEXTURE : TEXTURE_MAP.computeIfAbsent(new TypeSkin(fool.getStandType(), fool.getSkin()),
                StandEntityRenderer::typeSkinToTexture);
    }
}
