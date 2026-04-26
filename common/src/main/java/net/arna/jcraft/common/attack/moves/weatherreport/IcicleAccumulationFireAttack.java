package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.FiredIcicleProjectile;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.arna.jcraft.common.entity.projectile.WindGustEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class IcicleAccumulationFireAttack extends AbstractMove<IcicleAccumulationFireAttack, WeatherReportEntity> {

    private final float minScale;
    private final float maxScale;
    private final float minSpeed;
    private final float maxSpeed;

    public IcicleAccumulationFireAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                        final float minScale, final float maxScale,
                                        final float minSpeed, final float maxSpeed) {
        super(cooldown, windup, duration, moveDistance);
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide) return Set.of();
        WeatherReportEntity.ICICLE_CHARGE.discardChargeIcicle(attacker);
        final float chargeCompletion = Mth.clamp(getChargeTime() / (float) IcicleAccumulationChargeMove.MAX_CHARGE_TIME, 0.1f, 1.0f);

        final Vec3 look = user.getLookAngle();
        final float speed = minSpeed + chargeCompletion * (maxSpeed - minSpeed);
        final Vec3 velocity = look.scale(speed);

        final FiredIcicleProjectile icicle = new FiredIcicleProjectile(attacker.level(), user);
        icicle.setScale(minScale + chargeCompletion * (maxScale - minScale));

        icicle.setPos(user.getEyePosition());
        icicle.fire(velocity);

        attacker.level().addFreshEntity(icicle);

        if (attacker.isElectrified() && attacker.level() instanceof ServerLevel serverLevel) {
            final Vec3 launchPos = user.getEyePosition();
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, launchPos.x, launchPos.y, launchPos.z,
                    20, 0.3, 0.3, 0.3, 0.12);
            serverLevel.sendParticles(ParticleTypes.FLASH, launchPos.x, launchPos.y, launchPos.z,
                    1, 0, 0, 0, 0);
            serverLevel.playSound(null, launchPos.x, launchPos.y, launchPos.z,
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.35f, 1.5f);
            final double yaw = Math.atan2(-user.getLookAngle().x, user.getLookAngle().z);
            for (int side = -1; side <= 1; side += 2) {
                final double spreadAngle = yaw + side * (Math.PI / 4);
                final WindGustEntity gust = new WindGustEntity(attacker.level());
                gust.setMaster(user);
                gust.setVelocity(new Vec3(-Math.sin(spreadAngle), user.getLookAngle().y * 0.5, Math.cos(spreadAngle)));
                gust.setDamageValues(1.5f, 8);
                gust.setLarge(false);
                gust.setPos(launchPos.x, launchPos.y, launchPos.z);
                attacker.level().addFreshEntity(gust);
            }
        }

        return Set.of();
    }

    @Override
    public @NonNull MoveType<IcicleAccumulationFireAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull IcicleAccumulationFireAttack getThis() {
        return this;
    }

    @Override
    public @NonNull IcicleAccumulationFireAttack copy() {
        return copyExtras(new IcicleAccumulationFireAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                minScale, maxScale, minSpeed, maxSpeed));
    }

    public static class Type extends AbstractMove.Type<IcicleAccumulationFireAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<IcicleAccumulationFireAttack>, IcicleAccumulationFireAttack> buildCodec(RecordCodecBuilder.Instance<IcicleAccumulationFireAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new IcicleAccumulationFireAttack(cd, wu, dur, md, 0.4f, 1.0f, 0.5f, 1.75f));
        }
    }
}
