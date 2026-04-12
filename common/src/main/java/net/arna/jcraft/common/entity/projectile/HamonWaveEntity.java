package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.client.renderer.features.HamonParticlesFeatureRenderer;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HamonWaveEntity extends JAttackEntity {
    public final int LIFETIME = 30;
    public final float MAX_SIZE = 10.0f;
    private DamageSource damageSource = null;

    private final Set<UUID> hitEnemies = new HashSet<>();

    public HamonWaveEntity(Level world) {
        super(JEntityTypeRegistry.HAMON_WAVE.get(), world);
    }

    @Override
    public void setMaster(LivingEntity m) {
        super.setMaster(m);
        damageSource = level().damageSources().indirectMagic(m, this);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount > LIFETIME) {
            discard();
            return;
        }

        if (tickCount == 1) {
            playSound(JSoundRegistry.HAMON_RING.get(), 0.8f, 0.3f);
        }

        final float size = Mth.lerp((float) tickCount / LIFETIME, 0.0F, MAX_SIZE);
        final Vec3 position = position();
        final Direction gravity = GravityChangerAPI.getGravityDirection(this);

        if (level() instanceof ServerLevel serverLevel) {
            HamonSpec hamonSpec = null;

            if (master == null) {
                discard();
                return;
            } else if (JUtils.getSpec(master) instanceof HamonSpec typecheck) {
                hamonSpec = typecheck;
            } else {
                JCraft.LOGGER.warn("HamonWaveEntity exists for owner without Hamon!");
            }

            if (tickCount % 3 == 0) tryHit(size, position, gravity, serverLevel, hamonSpec);
        } else if (level() instanceof ClientLevel clientLevel) {
            final float numParticles = size * 10;

            HamonParticlesFeatureRenderer.prepareHamonAura(this);

            float arcRadius = size;

            for (int j = 0; j < 4; j++){
                final float arcOffset = random.nextFloat() * numParticles;

                for (int i = 0; i < (int) numParticles; i++) {
                    final float arcSize = (float) (random.nextGaussian() * 360.0F);
                    final int theta = Math.round((i + arcOffset) / numParticles * arcSize);
                    final float a = Mth.sin(Mth.DEG_TO_RAD * theta) * arcRadius;
                    final float b = Mth.cos(Mth.DEG_TO_RAD * theta) * arcRadius;

                    final Vec3 offset = RotationUtil.vecPlayerToWorld(new Vec3(a, random.nextGaussian() * 0.1, b), gravity);

                    clientLevel.addParticle(
                            j == 0 ? // Only the wavefront has aura
                            JUtils.chooseRandom(random,
                                    JParticleTypeRegistry.AURA_ARC.get(),
                                    JParticleTypeRegistry.AURA_BLOB.get(),
                                    JParticleTypeRegistry.HAMON_SPARK.get(),
                                    ParticleTypes.ELECTRIC_SPARK
                            ) :
                            JUtils.chooseRandom(random,
                                    JParticleTypeRegistry.HAMON_SPARK.get(),
                                    ParticleTypes.ELECTRIC_SPARK
                            ),
                            position.x + offset.x,
                            position.y + offset.y,
                            position.z + offset.z,
                            offset.x * 0.08,
                            offset.y * 0.08,
                            offset.z * 0.08
                    );
                }

                arcRadius -= 0.66;
            }
        }
    }

    protected void tryHit(float size, Vec3 position, Direction gravity, ServerLevel serverLevel, HamonSpec hamonSpec) {
        final List<Entity> potentialHits = serverLevel.getEntities(
                this,
                AABB.ofSize(position(), size * 2, size * 2, size * 2),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR
        );

        final List<Entity> targets = new ArrayList<>();

        for (Entity potentialHit : potentialHits) {
            final Entity entity = JUtils.getUserIfStand(potentialHit);
            if (targets.contains(entity)) continue;
            targets.add(entity);
        }

        boolean anyHit = false;

        for (Entity potentialHit : targets) {
            if (potentialHit instanceof LivingEntity living) {
                if (potentialHit.isPassengerOfSameVehicle(master)) continue;

                final Vec3 otherPos = living.position();
                double axisDistance;

                switch (gravity.getAxis()) {
                    case X -> axisDistance = Math.abs(otherPos.x - position.x);
                    case Y -> axisDistance = Math.abs(otherPos.y - position.y);
                    case Z -> axisDistance = Math.abs(otherPos.z - position.z);
                    default -> axisDistance = Double.MAX_VALUE;
                }

                // Vertically truncate hollow hitsphere
                if (axisDistance > 0.666) continue;

                final double distanceSqr = potentialHit.distanceToSqr(position);

                // Hollow hitsphere
                if (
                        distanceSqr < ((size - 0.5) * (size - 0.5)) ||
                        distanceSqr > ((size + 0.5) * (size + 0.5))
                ) {
                    continue;
                }

                Attacks.damageLogic(serverLevel, living, new AttackData(
                        living.position().subtract(position).scale(0.25),
                        13, StunType.LAUNCH.ordinal(), false, 3f, true, 6,
                        damageSource, master, CommonHitPropertyComponent.HitAnimation.LAUNCH,
                        null, false, false
                ));
                if (living instanceof Enemy) {
                    hitEnemies.add(living.getUUID());
                }

                if (hamonSpec != null) hamonSpec.processTarget(living);

                anyHit = true;
            }
        }

        if (anyHit) {
            playSound(JSoundRegistry.TWOH_CHARGE.get());
        }
    }

    @Override
    public void discard() {
        if (master instanceof final ServerPlayer player) {
            JAdvancementTriggerRegistry.HAMON5.trigger(player, hitEnemies.size());
        }
        super.discard();
    }

    // Physical Properties

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void push(@NotNull Entity entity) {
        // intentionally left empty
    }

    @Override
    protected void pushEntities() {
        // intentionally left empty
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return AABB.ofSize(position(), 0.0, 0.0, 0.0);
    }
}
