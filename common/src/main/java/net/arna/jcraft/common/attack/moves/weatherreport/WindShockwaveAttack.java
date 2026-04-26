package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.projectile.WindGustEntity;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WindShockwaveAttack extends AbstractBarrageAttack<WindShockwaveAttack, WeatherReportEntity> {

    public WindShockwaveAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final float damage, final int stun, final float hitboxSize, final float knockback,
                               final float offset, final int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    protected void processTarget(final WeatherReportEntity attacker, final LivingEntity target,
                                 final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        final LivingEntity user = attacker.getUser();
        if (user == null || attacker.level().isClientSide) return;

        final Vec3 windDir = user.getLookAngle();

        final AABB shockBox = AABB.ofSize(target.position(), 6, 4, 6);
        attacker.level().getEntitiesOfClass(LivingEntity.class, shockBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user && e != attacker.getBaseEntity()))
                .forEach(e -> {
                    e.setDeltaMovement(e.getDeltaMovement().add(windDir.scale(0.35)));
                    e.hurtMarked = true;
                });

        final WindGustEntity gust = new WindGustEntity(attacker.level());
        gust.setMaster(user);
        gust.setVelocity(windDir);
        gust.setDamageValues(getDamage() * 0.4f, 6);
        gust.setLarge(false);
        gust.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
        attacker.level().addFreshEntity(gust);
    }

    @Override
    public @NonNull MoveType<WindShockwaveAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WindShockwaveAttack getThis() {
        return this;
    }

    @Override
    public @NonNull WindShockwaveAttack copy() {
        return copyExtras(new WindShockwaveAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<WindShockwaveAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WindShockwaveAttack>, WindShockwaveAttack> buildCodec(RecordCodecBuilder.Instance<WindShockwaveAttack> instance) {
            return barrageDefault(instance, WindShockwaveAttack::new);
        }
    }
}
