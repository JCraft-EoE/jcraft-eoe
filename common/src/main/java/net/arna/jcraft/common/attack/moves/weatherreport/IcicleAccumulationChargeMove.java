package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.common.entity.projectile.LargeIcicleProjectile;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class IcicleAccumulationChargeMove extends AbstractHoldableMove<IcicleAccumulationChargeMove, WeatherReportEntity> {

    public static final int MAX_CHARGE_TIME = 60;

    public IcicleAccumulationChargeMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                                        final int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
        withArmor(2);
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        final LivingEntity user = attacker.getUser();
        if (user == null) return;

        final double completion = Math.min(getChargeTime() / (double) MAX_CHARGE_TIME, 1.0);

        if (attacker.level().isClientSide) {
            if (attacker.getRandom().nextDouble() > 0.3 - completion * 0.2) {
                attacker.level().addParticle(ParticleTypes.SNOWFLAKE,
                        attacker.getX() + attacker.getRandom().nextGaussian() * (0.3 + completion * 0.5),
                        attacker.getY() + attacker.getBbHeight() * 0.5 + attacker.getRandom().nextGaussian() * 0.3,
                        attacker.getZ() + attacker.getRandom().nextGaussian() * (0.3 + completion * 0.5),
                        -attacker.getRandom().nextGaussian() * 0.05,
                        -attacker.getRandom().nextGaussian() * 0.05,
                        -attacker.getRandom().nextGaussian() * 0.05);
            }
            return;
        }

        final float scale = 0.25f + (float) completion * 0.75f;
        final Vec3 iciclePos = user.getEyePosition().add(user.getLookAngle().scale(1.2 + completion));

        LargeIcicleProjectile icicle = attacker.getChargeIcicle();
        if (icicle == null || icicle.isRemoved()) {
            icicle = new LargeIcicleProjectile(attacker.level(), user);
            icicle.setManaged(true);
            attacker.level().addFreshEntity(icicle);
            attacker.setChargeIcicle(icicle);
        }
        icicle.setScale(scale);
        icicle.setPos(iciclePos);
        icicle.setDeltaMovement(Vec3.ZERO);

        final Vec3 look = user.getLookAngle();
        icicle.setYRot((float) (Math.atan2(look.x, look.z) * (180.0 / Math.PI)) + 180.0f);
        icicle.setXRot((float) (Math.atan2(look.y, look.horizontalDistance()) * (180.0 / Math.PI)));

        if (moveStun % 5 == 0) {
            final float touchDamage = 1.0f + (float) completion * 3.0f;
            final float radius = 0.8f + scale * 0.8f;
            final AABB box = AABB.ofSize(iciclePos, radius * 2, radius * 2, radius * 2);
            attacker.level().getEntitiesOfClass(LivingEntity.class, box,
                    EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user && e != attacker.getBaseEntity()))
                    .forEach(e -> {
                        final Vec3 push = e.position().subtract(iciclePos).normalize().scale(0.8);
                        e.setDeltaMovement(e.getDeltaMovement().add(push));
                        e.hurt(attacker.getDamageSource(), touchDamage);
                        e.hurtMarked = true;
                    });
        }
    }

    public void discardChargeIcicle(final WeatherReportEntity attacker) {
        final LargeIcicleProjectile icicle = attacker.getChargeIcicle();
        if (icicle != null && !icicle.isRemoved()) icicle.discard();
        attacker.setChargeIcicle(null);
    }

    @Override
    public @NonNull MoveType<IcicleAccumulationChargeMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull IcicleAccumulationChargeMove getThis() {
        return this;
    }

    @Override
    public @NonNull IcicleAccumulationChargeMove copy() {
        return copyExtras(new IcicleAccumulationChargeMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge()));
    }

    public static class Type extends AbstractHoldableMove.Type<IcicleAccumulationChargeMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<IcicleAccumulationChargeMove>, IcicleAccumulationChargeMove> buildCodec(RecordCodecBuilder.Instance<IcicleAccumulationChargeMove> instance) {
            return holdableDefault(instance, IcicleAccumulationChargeMove::new);
        }
    }
}
