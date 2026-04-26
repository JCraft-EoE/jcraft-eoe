package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class DryIceMove extends AbstractSimpleAttack<DryIceMove, WeatherReportEntity> {

    private static final int FREEZE_TICKS = 40;

    public DryIceMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                      final float damage, final int stun, final float hitboxSize, final float knockback,
                      final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        withHitSpark(null);
    }

    @Override
    protected void processTarget(final WeatherReportEntity attacker, final LivingEntity target,
                                 final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), FREEZE_TICKS * 2));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FREEZE_TICKS + 20, 3, false, false));

        if (attacker.level().isClientSide) {
            for (int i = 0; i < 12; i++) {
                final double angle = i * (Math.PI * 2 / 12);
                attacker.level().addParticle(ParticleTypes.SNOWFLAKE,
                        target.getX() + Math.cos(angle) * 0.5,
                        target.getY() + target.getBbHeight() * 0.5,
                        target.getZ() + Math.sin(angle) * 0.5,
                        Math.cos(angle) * 0.06, 0.04, Math.sin(angle) * 0.06);
            }
            for (int i = 0; i < 6; i++) {
                attacker.level().addParticle(ParticleTypes.SNOWFLAKE,
                        target.getX() + attacker.getRandom().nextGaussian() * 0.4,
                        target.getY() + attacker.getRandom().nextDouble() * target.getBbHeight(),
                        target.getZ() + attacker.getRandom().nextGaussian() * 0.4,
                        0, 0.02, 0);
            }
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (!attacker.level().isClientSide && attacker.level() instanceof ServerLevel serverLevel) {
            final Vec3 pos = attacker.position().add(user.getLookAngle().scale(1.5));
            for (int i = 0; i < 20; i++) {
                final double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
                final double r = attacker.getRandom().nextDouble() * getHitboxSize();
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        pos.x + Math.cos(angle) * r,
                        pos.y + attacker.getRandom().nextDouble() * 2,
                        pos.z + Math.sin(angle) * r,
                        1, 0.1, 0.05, 0.1, 0.02);
            }
        }

        return targets;
    }

    @Override
    public @NonNull MoveType<DryIceMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull DryIceMove getThis() {
        return this;
    }

    @Override
    public @NonNull DryIceMove copy() {
        return copyExtras(new DryIceMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<DryIceMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<DryIceMove>, DryIceMove> buildCodec(RecordCodecBuilder.Instance<DryIceMove> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off) ->
                    new DryIceMove(cd, wu, dur, md, 0f, 12, 2.5f, 0.1f, 0f));
        }
    }
}
