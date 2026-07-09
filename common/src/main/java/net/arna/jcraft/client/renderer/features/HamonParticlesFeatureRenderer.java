package net.arna.jcraft.client.renderer.features;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.client.particle.AuraArcParticle;
import net.arna.jcraft.client.particle.AuraBlobParticle;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin.client.AnimalModelAccessor;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Stream;

public class HamonParticlesFeatureRenderer<T extends LivingEntity, M extends AgeableListModel<T>> extends RenderLayer<T, M> {
    private static final Vector3f HAMON_COLOR = new Vector3f(0.8f, 0.4f, 0.2f);
    private static final Int2IntArrayMap lastTickCounts = new Int2IntArrayMap();

    public HamonParticlesFeatureRenderer(final EntityRendererProvider.Context context, final LivingEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    protected int getHamonCharge(final T entity) {
        final var hamon = JComponentPlatformUtils.getHamon(entity);
        if (!hamon.isHamonizeReady()) {
            return 0;
        }
        return Mth.ceil(hamon.getHamonCharge() / 5.0f);
    }

    public static void prepareHamonAura() {
        prepareHamonAura(null);
    }
    public static void prepareHamonAura(LivingEntity livingEntity) {
        if (livingEntity != null) HAMON_COLOR.y = 0.5f + livingEntity.getRandom().nextFloat() * 0.3f;

        AuraArcParticle.Factory.parent = livingEntity;
        AuraArcParticle.Factory.color = HAMON_COLOR;
        AuraBlobParticle.Factory.parent = livingEntity;
        AuraBlobParticle.Factory.color = HAMON_COLOR;
    }

    @Override
    public void render(final @NotNull PoseStack matrixStack, final @NotNull MultiBufferSource vertexConsumerProvider, final int packedLight,
                       final T livingEntity, final float limbSwing, final float limbSwingAmount, final float partialTick,
                       final float ageInTicks, final float netHeadYaw, final float headPitch) {
        final int id = livingEntity.getId();
        if (livingEntity.tickCount == lastTickCounts.get(id)) return;
        lastTickCounts.put(id, livingEntity.tickCount);

        final int m = getHamonCharge(livingEntity);
        if (m <= 0 || livingEntity.isSpectator()) {
            return;
        }

        final RandomSource random = livingEntity.getRandom();
        final AnimalModelAccessor accessor = (AnimalModelAccessor) getParentModel();
        final List<ModelPart> parts = Stream.concat(Streams.stream(accessor.callHeadParts()), Streams.stream(accessor.callBodyParts())).toList();
        final Level level = livingEntity.level();

        final var gravity = Vec3.atLowerCornerOf(
                GravityChangerAPI.getGravityDirection(livingEntity).getNormal()
        ).scale(livingEntity.getBoundingBox().getYsize());

        prepareHamonAura(livingEntity);

        for (int n = 0; n < m; ++n) {
            final ModelPart part = parts.get(random.nextInt(parts.size()));
            if (!part.isEmpty()) {
                ModelPart.Cube cube = part.getRandomCube(random);

                final float px = Mth.lerp(random.nextFloat(), cube.minX / 16.0F, cube.maxX / 16.0F);
                final float py = Mth.lerp(random.nextFloat(), cube.minY / 16.0F, cube.maxY / 16.0F);
                final float pz = Mth.lerp(random.nextFloat(), cube.minZ / 16.0F, cube.maxZ / 16.0F);

                Vector3f local = new Vector3f(px, py, pz);
                local.rotateX(part.xRot);
                local.rotateY(part.yRot);
                local.rotateZ(part.zRot);

                final float partX = part.x / 16.0F;
                final float partY = part.y / 16.0F;
                final float partZ = part.z / 16.0F;
                local.add(partX, partY, partZ);

                final float yaw = livingEntity.yBodyRot;
                final double cosYaw = Math.cos(Math.toRadians(yaw));
                final double sinYaw = Math.sin(Math.toRadians(yaw));

                final double worldX = local.x() * cosYaw - local.z() * sinYaw;
                final double worldZ = local.x() * sinYaw + local.z() * cosYaw;
                final double worldY = local.y();

                final double finalX = livingEntity.getX() - worldX;
                final double finalY = livingEntity.getY() - worldY;
                final double finalZ = livingEntity.getZ() - worldZ;

                level.addParticle(
                        JUtils.chooseRandom(random,
                                JParticleTypeRegistry.AURA_ARC.get(),
                                JParticleTypeRegistry.AURA_BLOB.get(),
                                JParticleTypeRegistry.HAMON_SPARK.get(),
                                ParticleTypes.ELECTRIC_SPARK
                                ),
                        // ParticleTypes.ELECTRIC_SPARK,
                        finalX - gravity.x(),
                        finalY - gravity.y(),
                        finalZ - gravity.z(),
                        0.0, 0.0, 0.0
                );
            }
        }
    }
}
