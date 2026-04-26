package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.projectile.HailProjectile;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class HailstormBarrageAttack extends AbstractBarrageAttack<HailstormBarrageAttack, WeatherReportEntity> {

    private final float projectileSpeed;
    private final float projectileInaccuracy;

    public HailstormBarrageAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                  final float damage, final int stun, final float hitboxSize, final float knockback,
                                  final float offset, final int interval,
                                  final float projectileSpeed, final float projectileInaccuracy) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.projectileSpeed = projectileSpeed;
        this.projectileInaccuracy = projectileInaccuracy;
        withHoldable(true);
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (!attacker.level().isClientSide || attacker.tickCount % 2 != 0) return;
        for (int i = 0; i < 3; i++) {
            final double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
            final double radius = 0.5 + attacker.getRandom().nextDouble() * 3.5;
            attacker.level().addParticle(ParticleTypes.CLOUD,
                    attacker.getX() + Math.cos(angle) * radius,
                    attacker.getY() + attacker.getBbHeight() + attacker.getRandom().nextDouble() * 2.0,
                    attacker.getZ() + Math.sin(angle) * radius,
                    0, 0.04, 0);
            attacker.level().addParticle(ParticleTypes.SNOWFLAKE,
                    attacker.getX() + attacker.getRandom().nextGaussian() * 2.5,
                    attacker.getY() + attacker.getBbHeight() * 0.5 + attacker.getRandom().nextDouble() * 2.0,
                    attacker.getZ() + attacker.getRandom().nextGaussian() * 2.5,
                    attacker.getRandom().nextGaussian() * 0.1, -0.2, attacker.getRandom().nextGaussian() * 0.1);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final var rng = attacker.getRandom();

        final Vec3 spawnPos = new Vec3(
                user.getX() + rng.nextGaussian() * 2.5,
                user.getY() + 5.5 + rng.nextDouble(),
                user.getZ() + rng.nextGaussian() * 2.5);
        final Vec3 targetPos = new Vec3(
                user.getX() + rng.nextGaussian() * 1.5,
                user.getY(),
                user.getZ() + rng.nextGaussian() * 1.5);
        final Vec3 dir = targetPos.subtract(spawnPos).normalize();

        final HailProjectile hail = new HailProjectile(attacker.level(), user);
        hail.setDamage(getDamage());
        hail.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        hail.shoot(dir.x, dir.y, dir.z, projectileSpeed, projectileInaccuracy);
        attacker.level().addFreshEntity(hail);

        return targets;
    }

    @Override
    public void onUserMoveInput(final WeatherReportEntity attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        super.onUserMoveInput(attacker, type, pressed, moveInitiated);
        if (type.getMoveClass() == getMoveClass() && !pressed) attacker.cancelMove();
    }

    @Override
    public @NonNull MoveType<HailstormBarrageAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull HailstormBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull HailstormBarrageAttack copy() {
        return copyExtras(new HailstormBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(),
                projectileSpeed, projectileInaccuracy));
    }

    public static class Type extends AbstractBarrageAttack.Type<HailstormBarrageAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HailstormBarrageAttack>, HailstormBarrageAttack> buildCodec(RecordCodecBuilder.Instance<HailstormBarrageAttack> instance) {
            return barrageDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off, interval) ->
                    new HailstormBarrageAttack(cd, wu, dur, md, dmg, st, hs, kb, off, interval, 1.5f, 0.3f));
        }
    }
}
