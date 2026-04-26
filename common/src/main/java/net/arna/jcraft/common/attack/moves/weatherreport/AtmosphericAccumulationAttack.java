package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.FrogRainCloudEntity;
import net.arna.jcraft.common.entity.projectile.StormCloudEntity;
import net.arna.jcraft.common.entity.projectile.WeatherTornadoEntity;
import net.arna.jcraft.common.entity.projectile.WinterStormEntity;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class AtmosphericAccumulationAttack extends AbstractMove<AtmosphericAccumulationAttack, WeatherReportEntity> {

    public static final int MAX_CHARGE_TICKS = 100;

    private final int maxChargeTicks;
    private boolean released;

    public AtmosphericAccumulationAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                         final int maxChargeTicks) {
        super(cooldown, windup, duration, moveDistance);
        this.maxChargeTicks = maxChargeTicks;
        withHoldable(true);
    }

    @Override
    public void onInitiate(final WeatherReportEntity attacker) {
        super.onInitiate(attacker);
        released = false;
        attacker.setWeatherMeter(0f);
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (released) return;

        if (!attacker.level().isClientSide) {
            final float newMeter = Mth.clamp(attacker.getWeatherMeter() + 1.2f / maxChargeTicks, 0f, 1f);
            attacker.setWeatherMeter(newMeter);
            if (moveStun == 1) {
                released = true;
                triggerPhenomenon(attacker);
            }
        } else if (attacker.tickCount % 2 == 0) {
            final LivingEntity user = attacker.getUser();
            if (user == null) return;
            final float density = attacker.getWeatherMeter();

            final int count = (int) (2 + density * 4);
            for (int i = 0; i < count; i++) {
                final double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
                final double radius = 3.0 + density * 8.0;
                final double heightVar = attacker.getRandom().nextGaussian() * (1.0 + density * 3.0);
                attacker.level().addParticle(ParticleTypes.CLOUD,
                        user.getX() + Math.cos(angle) * radius,
                        user.getY() + user.getBbHeight() + heightVar,
                        user.getZ() + Math.sin(angle) * radius,
                        Math.cos(angle + Math.PI / 2) * 0.08 * density,
                        0.06,
                        Math.sin(angle + Math.PI / 2) * 0.08 * density);
            }

            if (density >= 0.5f) {
                final double a = attacker.getRandom().nextDouble() * Math.PI * 2;
                final double r = 4.0 + density * 6.0;
                attacker.level().addParticle(
                        density >= 0.75f ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SNOWFLAKE,
                        user.getX() + Math.cos(a) * r,
                        user.getY() + user.getBbHeight() + density * 3,
                        user.getZ() + Math.sin(a) * r,
                        0, 0.08, 0);
            }
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public void onUserMoveInput(final WeatherReportEntity attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        if (!pressed && type.getMoveClass() == getMoveClass() && moveInitiated) {
            released = true;
            triggerPhenomenon(attacker);
            attacker.cancelMove();
        }
    }

    private void triggerPhenomenon(final WeatherReportEntity attacker) {
        if (attacker.level().isClientSide) return;
        final LivingEntity user = attacker.getUser();
        if (user == null) return;

        final float charge = attacker.getWeatherMeter();

        if (charge >= 1.0f) {
            spawnFrogRainClouds(attacker, user);
        } else if (charge >= 0.75f) {
            spawnStormClouds(attacker, user);
        } else if (charge >= 0.5f) {
            spawnWinterStorm(attacker, user);
        } else if (charge >= 0.25f) {
            spawnTornados(attacker, user);
        }

        attacker.setWeatherMeter(0f);
    }

    private void spawnTornados(final WeatherReportEntity attacker, final LivingEntity user) {
        for (int i = 0; i < 2; i++) {
            final double angle = Math.PI * i;
            final Vec3 pos = user.position().add(Math.cos(angle) * 10, 0, Math.sin(angle) * 10);
            final WeatherTornadoEntity tornado = new WeatherTornadoEntity(attacker.level());
            tornado.setMaster(attacker.getUserOrThrow());
            tornado.setElectrified(attacker.isElectrified());
            tornado.setPos(pos);
            attacker.level().addFreshEntity(tornado);
        }
    }

    private void spawnWinterStorm(final WeatherReportEntity attacker, final LivingEntity user) {
        final WinterStormEntity storm = new WinterStormEntity(attacker.level());
        storm.setMaster(attacker.getUserOrThrow());
        storm.setPos(user.position());
        attacker.level().addFreshEntity(storm);
    }

    private void spawnStormClouds(final WeatherReportEntity attacker, final LivingEntity user) {
        for (int i = 0; i < 8; i++) {
            final double angle = (Math.PI * 2 / 8) * i;
            final Vec3 pos = user.position().add(Math.cos(angle) * 6, 0, Math.sin(angle) * 6);
            final StormCloudEntity cloud = new StormCloudEntity(attacker.level());
            cloud.setMaster(attacker.getUserOrThrow());
            cloud.setPos(pos);
            attacker.level().addFreshEntity(cloud);
        }
    }

    private void spawnFrogRainClouds(final WeatherReportEntity attacker, final LivingEntity user) {
        for (int i = 0; i < 3; i++) {
            final double angle = (Math.PI * 2 / 3) * i;
            final Vec3 pos = user.position().add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8);
            final FrogRainCloudEntity cloud = new FrogRainCloudEntity(attacker.level());
            cloud.setMaster(attacker.getUserOrThrow());
            cloud.setPos(pos);
            attacker.level().addFreshEntity(cloud);
        }
    }

    @Override
    public @NonNull MoveType<AtmosphericAccumulationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull AtmosphericAccumulationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull AtmosphericAccumulationAttack copy() {
        return copyExtras(new AtmosphericAccumulationAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), maxChargeTicks));
    }

    public static class Type extends AbstractMove.Type<AtmosphericAccumulationAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<AtmosphericAccumulationAttack>, AtmosphericAccumulationAttack> buildCodec(RecordCodecBuilder.Instance<AtmosphericAccumulationAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new AtmosphericAccumulationAttack(cd, wu, dur, md, MAX_CHARGE_TICKS));
        }
    }
}
