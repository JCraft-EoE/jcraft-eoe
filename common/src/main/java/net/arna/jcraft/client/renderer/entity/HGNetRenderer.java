package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.renderer.entity.layer.HGNetGlowLayer;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.IntStream;

/**
 * The {@link AbstractEntityRenderer} for {@link HGNetEntity}.
 */
@Environment(EnvType.CLIENT)
public class HGNetRenderer extends AbstractEntityRenderer<HGNetEntity> {
    protected static final List<ResourceLocation> SKINS = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/hg_nets/" + i + ".png")).toList();

    public static final String ID = "hg_nets";

    public HGNetRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.<HGNetEntity>builder(
                entity -> JCraft.id(AbstractEntityRenderer.MODEL_STR_TEMPLATE.formatted(ID)),
                entity -> SKINS.get(entity.getSkin()))
                .setAnimatorProvider(() -> new EntityAnimator<>(ID))
                .setRenderEntry((pc) -> {
                    final HGNetEntity entity = pc.animatable();

                    if (entity.tickCount < 5) {
                        HGNetEntity.SPAWN.sendForEntity(entity);
                    } else {
                        if (entity.getState() == 3) {
                            HGNetEntity.WILT.sendForEntity(entity);
                        } else if (entity.getState() == 2) {
                            HGNetEntity.CONSTRICT.sendForEntity(entity);
                        } else {
                            HGNetEntity.IDLE.sendForEntity(entity);
                        }
                    }

                    return pc;
                })
                .addRenderLayer(new HGNetGlowLayer())
                .setShadowRadius(1.25f)
                .build(),
                context, ID);
    }

    @Override
    public boolean shouldShowName(final @NonNull HGNetEntity animatable) {
        return false;
    }
}
