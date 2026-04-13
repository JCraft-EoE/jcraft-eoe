package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public final class FlashbangAttack extends AbstractMove<FlashbangAttack, SpeedKingEntity> {
    /** Damage dealt to tagged entities on detonation */
    private final float detonateTagDamage;
    /** Knockback strength applied to tagged entities on detonation */
    private final double detonateKnockback;
    /** Radius (blocks) to search for entities/blocks to tag */
    private final int tagRadius;
    /** Ticks between particle updates on tagged targets */
    private final int particleInterval;
    /** Distance in front of the user where the flashbang is placed */
    private final double spawnDistance;
    /** Blindness effect duration (ticks) on tagged entities */
    private final int blindnessDuration;
    /** Confusion/nausea effect duration (ticks) on tagged entities */
    private final int confusionDuration;
    /** Blast radius for untagged entities at the detonation point */
    private final double blastRadius;

    private static final Map<String, FlashbangTimer> ACTIVE_TIMERS = new HashMap<>();

    public FlashbangAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float detonateTagDamage, final double detonateKnockback,
                           final int tagRadius, final int particleInterval,
                           final double spawnDistance, final int blindnessDuration,
                           final int confusionDuration, final double blastRadius) {
        super(cooldown, windup, duration, moveDistance);
        this.detonateTagDamage = detonateTagDamage;
        this.detonateKnockback = detonateKnockback;
        this.tagRadius = tagRadius;
        this.particleInterval = particleInterval;
        this.spawnDistance = spawnDistance;
        this.blindnessDuration = blindnessDuration;
        this.confusionDuration = confusionDuration;
        this.blastRadius = blastRadius;
    }

    @Override
    public @NonNull MoveType<FlashbangAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        final Level level = attacker.level();
        final Vec3 userPos = user.position();
        final Vec3 lookDir = user.getLookAngle();
        final Vec3 targetPos = userPos.add(lookDir.scale(spawnDistance));

        // --- Tag nearby entities ---
        List<UUID> taggedEntityIds = new ArrayList<>();
        AABB tagBox = new AABB(targetPos.add(-tagRadius, -tagRadius, -tagRadius),
                               targetPos.add(tagRadius, tagRadius, tagRadius));
        level.getEntitiesOfClass(LivingEntity.class, tagBox,
                e -> e != user && e != attacker && e.isAlive() && !e.isSpectator())
                .forEach(e -> taggedEntityIds.add(e.getUUID()));

        // --- Tag nearby blocks (solid, non-air within tagRadius) ---
        List<BlockPos> taggedBlocks = new ArrayList<>();
        BlockPos center = BlockPos.containing(targetPos);
        for (int dx = -tagRadius; dx <= tagRadius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -tagRadius; dz <= tagRadius; dz++) {
                    BlockPos bp = center.offset(dx, dy, dz);
                    if (!level.getBlockState(bp).isAir() && level.getBlockState(bp).isSolid()) {
                        taggedBlocks.add(bp);
                    }
                }
            }
        }

        // Unique key per flashbang instance
        String timerKey = level.dimension().location() + "_" + level.getGameTime() + "_" + userPos.hashCode();
        ACTIVE_TIMERS.put(timerKey, new FlashbangTimer(
                targetPos,
                level.getGameTime() + getDuration(),
                user.getUUID(),
                lookDir.normalize(),
                taggedEntityIds,
                taggedBlocks,
                detonateTagDamage,
                detonateKnockback,
                particleInterval,
                blindnessDuration,
                confusionDuration,
                blastRadius
        ));

        return Set.of();
    }

    public static void tickTimers(Level level) {
        if (level.isClientSide()) return;
        final ServerLevel serverLevel = (ServerLevel) level;
        final long now = level.getGameTime();

        ACTIVE_TIMERS.entrySet().removeIf(entry -> {
            FlashbangTimer timer = entry.getValue();

            if (now >= timer.explodeTime) {
                detonate(serverLevel, timer);
                return true;
            }

            // Spawn KQ-style orbit particles around each tagged target
            if (now % timer.particleInterval == 0) {
                spawnTagParticles(serverLevel, timer, now);
            }

            return false;
        });
    }

    /** Spawns WAX_ON / WITCH orbit particles around tagged entities and blocks,
     *  mirroring the KQ bomb tracker visual. */
    private static void spawnTagParticles(ServerLevel level, FlashbangTimer timer, long now) {
        // Entities: WAX_ON particles
        for (UUID id : timer.taggedEntityIds) {
            var entity = level.getEntity(id);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) continue;

            Vec3 pos = target.position();
            double w = target.getBbWidth();
            double h = target.getBbHeight();
            for (int i = 0; i < 3; i++) {
                double ox = (level.random.nextDouble() - 0.5) * w * 1.5;
                double oy = level.random.nextDouble() * h;
                double oz = (level.random.nextDouble() - 0.5) * w * 1.5;
                level.sendParticles(ParticleTypes.WAX_ON,
                        pos.x + ox, pos.y + oy, pos.z + oz,
                        1, 0, 0.05, 0, 0.05);
            }
        }

    }

    private static void detonate(ServerLevel level, FlashbangTimer timer) {
        Vec3 pos = timer.detonatePos;

        // TNT-style explosion for visual + sound — no block damage
        level.explode(null, pos.x, pos.y, pos.z, 3.0f, false, Level.ExplosionInteraction.NONE);

        Entity userEntity = level.getEntity(timer.userUUID);

        // Knockback direction: primarily the user's look direction with a slight upward lift
        Vec3 lookDir = timer.userLookDir;
        Vec3 kbBase = new Vec3(lookDir.x, 0.4, lookDir.z).normalize().scale(timer.detonateKnockback);

        // Detonate tagged entities
        for (UUID id : timer.taggedEntityIds) {
            var entity = level.getEntity(id);
            if (!(entity instanceof LivingEntity target) || !target.isAlive()) continue;

            target.hurt(target.damageSources().explosion(null, userEntity), timer.detonateTagDamage);

            target.setDeltaMovement(target.getDeltaMovement().add(kbBase));
            target.hurtMarked = true;

            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, timer.blindnessDuration, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, timer.confusionDuration, 0, false, true));
        }

        // Hit any untagged entities at blast center
        AABB blastBox = new AABB(
                pos.add(-timer.blastRadius, -timer.blastRadius, -timer.blastRadius),
                pos.add(timer.blastRadius,   timer.blastRadius,  timer.blastRadius));
        level.getEntitiesOfClass(LivingEntity.class, blastBox,
                e -> e.isAlive() && !e.isSpectator() && !timer.taggedEntityIds.contains(e.getUUID()))
                .forEach(e -> {
                    e.hurt(e.damageSources().explosion(null, userEntity),
                            timer.detonateTagDamage * 0.5f);
                    // Knockback untagged away from blast center
                    Vec3 awayKb = e.position().subtract(pos).normalize().scale(timer.detonateKnockback * 0.6);
                    e.setDeltaMovement(e.getDeltaMovement().add(awayKb.x, 0.3, awayKb.z));
                    e.hurtMarked = true;
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
                            timer.blindnessDuration / 2, 0, false, true));
                });
    }

    @Override
    protected @NonNull FlashbangAttack getThis() { return this; }

    @Override
    public @NonNull FlashbangAttack copy() {
        return copyExtras(new FlashbangAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                detonateTagDamage, detonateKnockback, tagRadius, particleInterval,
                spawnDistance, blindnessDuration, confusionDuration, blastRadius));
    }

    public record FlashbangTimer(
            Vec3 detonatePos,
            long explodeTime,
            UUID userUUID,
            Vec3 userLookDir,
            List<UUID> taggedEntityIds,
            List<BlockPos> taggedBlocks,
            float detonateTagDamage,
            double detonateKnockback,
            int particleInterval,
            int blindnessDuration,
            int confusionDuration,
            double blastRadius
    ) {}

    public static class Type extends AbstractMove.Type<FlashbangAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlashbangAttack>, FlashbangAttack> buildCodec(RecordCodecBuilder.Instance<FlashbangAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new FlashbangAttack(cd, wu, dur, md, 10.0f, 2.0, 4, 3, 2.5, 120, 80, 2.0));
        }
    }
}
