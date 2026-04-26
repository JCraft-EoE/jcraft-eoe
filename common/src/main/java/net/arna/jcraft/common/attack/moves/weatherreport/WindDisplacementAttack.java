package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WindGustEntity;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class WindDisplacementAttack extends AbstractSimpleAttack<WindDisplacementAttack, WeatherReportEntity> {

    public WindDisplacementAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                  final float damage, final int stun, final float hitboxSize, final float knockback,
                                  final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        spawnWindGust(attacker, user, false);
        return targets;
    }

    private static void spawnWindGust(final WeatherReportEntity attacker, final LivingEntity user, final boolean large) {
        if (attacker.level().isClientSide) return;
        final WindGustEntity gust = new WindGustEntity(attacker.level());
        gust.setMaster(user);
        gust.setVelocity(user.getLookAngle());
        gust.setDamageValues(attacker.isElectrified() ? 2.0f : 1.5f, 10);
        gust.setLarge(large);
        gust.setPos(user.getEyePosition().subtract(0, 0.5, 0));
        attacker.level().addFreshEntity(gust);
    }

    @Override
    public @NonNull MoveType<WindDisplacementAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WindDisplacementAttack getThis() {
        return this;
    }

    @Override
    public @NonNull WindDisplacementAttack copy() {
        return copyExtras(new WindDisplacementAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<WindDisplacementAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WindDisplacementAttack>, WindDisplacementAttack> buildCodec(RecordCodecBuilder.Instance<WindDisplacementAttack> instance) {
            return attackDefault(instance, WindDisplacementAttack::new);
        }
    }
}
