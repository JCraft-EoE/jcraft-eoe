package net.arna.jcraft.client.renderer.entity.projectiles;

import lombok.NonNull;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import mod.azure.azurelib.render.entity.AzEntityRendererPipeline;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.RazorProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * The {@link ProjectileRenderer} for {@link RazorProjectile}.
 */
@Environment(EnvType.CLIENT)
public class RazorRenderer extends ProjectileRenderer<RazorProjectile> {

    protected static final Map<Integer, ResourceLocation> SKINS = Map.of(
            0, JCraft.id("textures/entity/projectiles/razor.png"),
            1, JCraft.id("textures/entity/projectiles/nail.png"),
            2, JCraft.id("textures/entity/projectiles/scissors.png"));

    public static final String ID = "razor";

    public RazorRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.builder(
                entity -> JCraft.id(MODEL_STR_TEMPLATE.formatted(ID)),
                RazorRenderer::getTexture
                )
                .setRenderType(entity -> RenderType.entityTranslucent(getTexture(entity)))
                .setModelRenderer((pipeline, layer) ->
                        new ProjectileModelRenderer<>((AzEntityRendererPipeline<RazorProjectile>) pipeline, layer))
                .build(),
                context, ID);
    }

    protected static ResourceLocation getTexture(final @NonNull RazorProjectile razor) {
        return SKINS.get(razor.getId() % 3);
    }

}