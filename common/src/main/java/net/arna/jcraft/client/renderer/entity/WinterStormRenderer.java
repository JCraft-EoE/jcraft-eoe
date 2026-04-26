package net.arna.jcraft.client.renderer.entity;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.WinterStormEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class WinterStormRenderer extends AbstractEntityRenderer<WinterStormEntity> {

    private static final ResourceLocation MODEL = JCraft.id("geo/weather_report.geo.json");
    private static final ResourceLocation TEXTURE = JCraft.id("textures/entity/stands/weather_report/weather_report.png");

    public WinterStormRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, () -> new EntityAnimator<>(JCraft.id("animations/weather_report.animation.json")), MODEL, TEXTURE);
    }

    @Override
    public boolean shouldShowName(final @NonNull WinterStormEntity animatable) {
        return false;
    }
}
