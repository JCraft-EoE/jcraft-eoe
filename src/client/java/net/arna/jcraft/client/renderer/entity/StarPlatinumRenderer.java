package net.arna.jcraft.client.renderer.entity;

import net.arna.jcraft.client.model.entity.StarPlatinumModel;
import net.arna.jcraft.common.entity.StarPlatinumEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class StarPlatinumRenderer extends GeoEntityRenderer<StarPlatinumEntity> {

    private int currentTick = -1;

    public StarPlatinumRenderer(EntityRendererFactory.Context context) {
        super(context, new StarPlatinumModel("textures/entity/starplatinum.png", "animations/starplatinum.animation.json"));
    }

    @Override
    public RenderLayer getRenderType(StarPlatinumEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (mcClient.player.getFirstPassenger() == animatable)
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));

        return RenderLayer.getEntityCutout(this.getTextureLocation(animatable));
    }

    @Override
    public void render(GeoModel model, StarPlatinumEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (((IEntityDataSaver) mcClient.player).getStand() == animatable)
                a = animatable.getAlpha();

        // Render inhale air effects
        if (animatable.getInhaleTime() > 0 && currentTick != animatable.age) {
            currentTick = animatable.age;
            if (model.getBone("head").isPresent()) {
                Random random = animatable.getRandom();
                Vec3d rotVec = animatable.getRotationVector().multiply(2.0);
                GeoBone headBone = model.getBone("head").get();

                Vector3d particlePos = headBone.getWorldPosition();
                Vec3d addVel = new Vec3d(rotVec.x + random.nextDouble() - 0.5, rotVec.y + random.nextDouble() - 0.5, rotVec.z + random.nextDouble() - 0.5);
                particlePos.x += addVel.x;
                particlePos.y += addVel.y;
                particlePos.z += addVel.z;

                animatable.getEntityWorld().addParticle(ParticleTypes.POOF,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
            }
        }

        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, a);
    }
}