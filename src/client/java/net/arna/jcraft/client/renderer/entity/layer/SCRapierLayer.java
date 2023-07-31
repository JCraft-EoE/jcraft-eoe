package net.arna.jcraft.client.renderer.entity.layer;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.SilverChariotEntity;
import net.arna.jcraft.common.entity.StandType;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.List;
import java.util.stream.IntStream;

import static net.arna.jcraft.common.entity.projectile.RapierProjectile.ARMOR_OFF_TEXTURE;
import static net.arna.jcraft.common.entity.projectile.RapierProjectile.POSSESSED_TEXTURE;

public class SCRapierLayer extends GeoLayerRenderer<SilverChariotEntity> {
    private static final Identifier MODEL = JCraft.id("geo/silver_chariot.geo.json");
    private static List<Identifier> skins;
    public SCRapierLayer(IGeoRenderer<SilverChariotEntity> entityRendererIn) {
        super(entityRendererIn);

        skins = IntStream.rangeClosed(0, StandType.SILVER_CHARIOT.getSkinCount())
                .mapToObj(i -> JCraft.id("textures/entity/stands/silver_chariot/rapier_" + (i == 0 ? "default" : "skin" + i) + ".png"))
                .toList();
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, SilverChariotEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entitylivingbaseIn.hasRapier()) {
            int mode = entitylivingbaseIn.getMode();

            RenderLayer cameo = RenderLayer.getArmorCutoutNoCull(
                    mode == 3 ? POSSESSED_TEXTURE :
                            mode == 2 ? ARMOR_OFF_TEXTURE :
                                    skins.get(entitylivingbaseIn.getSkin())
            );

            matrixStackIn.push();
            getRenderer().render(getEntityModel().getModel(MODEL), entitylivingbaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                    bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
            matrixStackIn.pop();
        }
    }
}
