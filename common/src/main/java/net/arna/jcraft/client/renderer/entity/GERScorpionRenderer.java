package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * The {@link AbstractEntityRenderer} for {@link GERScorpionEntity}.
 */
@Environment(EnvType.CLIENT)
public class GERScorpionRenderer extends AbstractEntityRenderer<GERScorpionEntity> {
    
    public static final String ID = "gerscorpion";

    private static final ResourceLocation ROCK = JCraft.id("textures/entity/rock.png");
    private static final ResourceLocation TEXTURE = JCraft.id("textures/entity/" + ID + ".png");

    public GERScorpionRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.<GERScorpionEntity>builder(
                entity -> JCraft.id(MODEL_STR_TEMPLATE.formatted(ID)),
                entity -> entity.isRock() ? ROCK : TEXTURE
                )
                .setAnimatorProvider(() -> new EntityAnimator<>(ID))
                .build(),
                context, ID);
    }
}
