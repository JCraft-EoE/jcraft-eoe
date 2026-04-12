package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.RapierProjectile;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The {@link ProjectileRenderer} for {@link RapierProjectile}.
 */
public class RapierRenderer extends ProjectileRenderer<RapierProjectile> {
    public static final List<ResourceLocation> SKINS = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/stands/silver_chariot/rapier_" + (i == 0 ? "default" : "skin" + i) + ".png")).toList();

    public final static List<ResourceLocation> POSSESSED_SKINS = IntStream.rangeClosed(0, 4)
            .mapToObj(i -> JCraft.id("textures/entity/stands/silver_chariot/rapier_possessed" + i + ".png"))
            .toList();

    public static final ResourceLocation ARMOR_OFF_TEXTURE = JCraft.id("textures/entity/stands/silver_chariot/rapier_no_armor.png");

    public static final String ID = "rapier";

    public RapierRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.builder(
                entity -> JCraft.id(MODEL_STR_TEMPLATE.formatted(ID)),
                RapierRenderer::getTexture
                )
                .setRenderType(entity -> RenderType.entityTranslucent(getTexture(entity)))
                .setAnimatorProvider(() -> new EntityAnimator<>(ID))
                .setModelRenderer((pipeline, layer) ->
                        new ProjectileModelRenderer<>((AzEntityRendererPipeline<RapierProjectile>) pipeline, layer))
                .build(),
                context, ID);
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(final @NonNull RapierProjectile rapier) {
        return getTexture(rapier);
    }

    protected static ResourceLocation getTexture(final @NonNull RapierProjectile rapier) {
        final int skin = rapier.getSkin();
        return switch (skin) {
            case -2 -> POSSESSED_SKINS.get(0);
            case -1 -> ARMOR_OFF_TEXTURE;
            default -> SKINS.get(skin);
        };
    }

}
