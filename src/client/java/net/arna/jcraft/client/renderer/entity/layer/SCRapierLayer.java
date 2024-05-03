package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;
import java.util.stream.IntStream;

import static net.arna.jcraft.common.entity.projectile.RapierProjectile.ARMOR_OFF_TEXTURE;
import static net.arna.jcraft.common.entity.projectile.RapierProjectile.POSSESSED_TEXTURE;

public class SCRapierLayer extends GeoRenderLayer<SilverChariotEntity> {
    private static final Identifier MODEL = JCraft.id("geo/silver_chariot.geo.json");
    private static List<Identifier> skins;
    public SCRapierLayer(GeoRenderer<SilverChariotEntity> entityRendererIn) {
        super(entityRendererIn);

        skins = IntStream.rangeClosed(0, StandType.SILVER_CHARIOT.getSkinCount())
                .mapToObj(i -> JCraft.id("textures/entity/stands/silver_chariot/rapier_" + (i == 0 ? "default" : "skin" + i) + ".png"))
                .toList();
    }

    @Override
    public void render(MatrixStack matrixStackIn, SilverChariotEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferIn, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
        if (animatable.hasRapier()) {
            SilverChariotEntity.Mode mode = animatable.getMode();

            RenderLayer cameo = RenderLayer.getArmorCutoutNoCull(
                    mode == SilverChariotEntity.Mode.POSSESSED ? POSSESSED_TEXTURE :
                            mode == SilverChariotEntity.Mode.ARMORLESS ? ARMOR_OFF_TEXTURE :
                                    skins.get(animatable.getSkin())
            );

            matrixStackIn.push();
            getRenderer().reRender(getDefaultBakedModel(animatable), matrixStackIn, bufferIn, animatable, cameo,
                    bufferIn.getBuffer(cameo), partialTicks, packedLightIn, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1f);
            matrixStackIn.pop();
        }
    }
}
