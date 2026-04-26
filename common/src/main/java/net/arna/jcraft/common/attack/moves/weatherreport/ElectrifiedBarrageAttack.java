package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ElectrifiedBarrageAttack extends AbstractBarrageAttack<ElectrifiedBarrageAttack, WeatherReportEntity> {

    private final int dotDuration;
    private final int dotAmplifier;

    public ElectrifiedBarrageAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                    final float damage, final int stun, final float hitboxSize, final float knockback,
                                    final float offset, final int interval, final int dotDuration, final int dotAmplifier) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.dotDuration = dotDuration;
        this.dotAmplifier = dotAmplifier;
    }

    private static final float FORK_RANGE = 4f;

    @Override
    protected void processTarget(final WeatherReportEntity attacker, final LivingEntity target,
                                 final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, dotDuration, dotAmplifier, true, false));
        if (attacker.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                attacker.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        target.getX() + target.getRandom().nextGaussian() * 0.3,
                        target.getY() + target.getBbHeight() * 0.5 + target.getRandom().nextGaussian() * 0.3,
                        target.getZ() + target.getRandom().nextGaussian() * 0.3,
                        0, 0, 0);
            }
        }

        if (attacker.level().isClientSide) return;
        final LivingEntity user = attacker.getUser();
        final List<LivingEntity> forkCandidates = attacker.level().getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(target.position(), FORK_RANGE * 2, FORK_RANGE * 2, FORK_RANGE * 2),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != target && e != user && e != attacker.getBaseEntity()));
        if (forkCandidates.isEmpty()) return;

        forkCandidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(target)));
        final LivingEntity forkTarget = forkCandidates.get(0);
        forkTarget.hurt(damageSource, getDamage() * 0.5f);
        forkTarget.addEffect(new MobEffectInstance(MobEffects.POISON, dotDuration / 2, dotAmplifier, true, false));

        if (attacker.level() instanceof ServerLevel serverLevel) {
            final Vec3 from = target.getEyePosition();
            final Vec3 to = forkTarget.getEyePosition();
            final Vec3 dir = to.subtract(from);
            final Vec3 perp = (Math.abs(dir.y / Math.max(dir.length(), 1e-4)) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0))
                    .cross(dir).normalize();
            Vec3 prev = from;
            for (int i = 1; i <= 5; i++) {
                final double t = (double) i / 5;
                final double jag = i < 5 ? (attacker.getRandom().nextDouble() - 0.5) * dir.length() * 0.25 : 0;
                final Vec3 wp = from.add(dir.scale(t)).add(perp.scale(jag));
                final Vec3 seg = wp.subtract(prev);
                final int steps = Math.max(1, (int) (seg.length() / 0.3));
                for (int j = 0; j <= steps; j++) {
                    final Vec3 p = prev.add(seg.scale((double) j / steps));
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.01);
                }
                prev = wp;
            }
        }
    }

    @Override
    public @NonNull MoveType<ElectrifiedBarrageAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull ElectrifiedBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ElectrifiedBarrageAttack copy() {
        return copyExtras(new ElectrifiedBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), dotDuration, dotAmplifier));
    }

    public static class Type extends AbstractBarrageAttack.Type<ElectrifiedBarrageAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ElectrifiedBarrageAttack>, ElectrifiedBarrageAttack> buildCodec(RecordCodecBuilder.Instance<ElectrifiedBarrageAttack> instance) {
            return barrageDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off, interval) ->
                    new ElectrifiedBarrageAttack(cd, wu, dur, md, dmg, st, hs, kb, off, interval, 60, 0));
        }
    }
}
