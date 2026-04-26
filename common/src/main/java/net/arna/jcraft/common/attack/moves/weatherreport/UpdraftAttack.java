package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class UpdraftAttack extends AbstractSimpleAttack<UpdraftAttack, WeatherReportEntity> {

    private final float updraftRadius;
    private final float launchVelocity;

    public UpdraftAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                         final float damage, final int stun, final float hitboxSize, final float knockback,
                         final float offset, final float updraftRadius, final float launchVelocity) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.updraftRadius = updraftRadius;
        this.launchVelocity = launchVelocity;
        withHitSpark(null);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (!attacker.level().isClientSide) {
            final AABB column = AABB.ofSize(user.position(), updraftRadius * 2, 4, updraftRadius * 2);
            attacker.level().getEntitiesOfClass(LivingEntity.class, column,
                    EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user && e != attacker.getBaseEntity()))
                    .forEach(e -> {
                        final Vec3 vel = e.getDeltaMovement();
                        e.setDeltaMovement(vel.x * 0.4, launchVelocity, vel.z * 0.4);
                        e.hurtMarked = true;
                    });

            final Vec3 userVel = user.getDeltaMovement();
            user.setDeltaMovement(userVel.x, Math.max(userVel.y, launchVelocity * 0.55f), userVel.z);
            user.hurtMarked = true;
        }

        if (attacker.level().isClientSide) {
            for (int i = 0; i < 24; i++) {
                final double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
                final double r = attacker.getRandom().nextDouble() * updraftRadius;
                attacker.level().addParticle(ParticleTypes.CLOUD,
                        user.getX() + Math.cos(angle) * r,
                        user.getY() + attacker.getRandom().nextDouble() * 2.5,
                        user.getZ() + Math.sin(angle) * r,
                        (Math.cos(angle) * 0.05), 0.12, (Math.sin(angle) * 0.05));
                attacker.level().addParticle(ParticleTypes.POOF,
                        user.getX() + Math.cos(angle) * r * 0.6,
                        user.getY() + attacker.getRandom().nextDouble() * 1.5,
                        user.getZ() + Math.sin(angle) * r * 0.6,
                        0, 0.08, 0);
            }
        } else if (attacker.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 16; i++) {
                final double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
                final double r = attacker.getRandom().nextDouble() * updraftRadius;
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        user.getX() + Math.cos(angle) * r,
                        user.getY() + attacker.getRandom().nextDouble() * 3.0,
                        user.getZ() + Math.sin(angle) * r,
                        1, 0.05, 0.1, 0.05, 0.04);
            }
        }

        return targets;
    }

    @Override
    public @NonNull MoveType<UpdraftAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull UpdraftAttack getThis() {
        return this;
    }

    @Override
    public @NonNull UpdraftAttack copy() {
        return copyExtras(new UpdraftAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), updraftRadius, launchVelocity));
    }

    public static class Type extends AbstractSimpleAttack.Type<UpdraftAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<UpdraftAttack>, UpdraftAttack> buildCodec(RecordCodecBuilder.Instance<UpdraftAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off) ->
                    new UpdraftAttack(cd, wu, dur, md, dmg, st, hs, kb, off, 2.5f, 1.35f));
        }
    }
}
