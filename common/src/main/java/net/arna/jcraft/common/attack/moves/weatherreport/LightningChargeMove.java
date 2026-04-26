package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class LightningChargeMove extends AbstractMove<LightningChargeMove, WeatherReportEntity> {

    private final int chargeDurationTicks;

    public LightningChargeMove(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final int chargeDurationTicks) {
        super(cooldown, windup, duration, moveDistance);
        this.chargeDurationTicks = chargeDurationTicks;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        attacker.setElectrifiedTicks(chargeDurationTicks);

        if (attacker.level() instanceof ServerLevel serverLevel) {
            final double x = attacker.getX(), y = attacker.getY(), z = attacker.getZ();
            for (int i = 0; i < 24; i++) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x + attacker.getRandom().nextGaussian() * 0.6,
                        y + attacker.getBbHeight() * 0.5 + attacker.getRandom().nextGaussian() * 0.5,
                        z + attacker.getRandom().nextGaussian() * 0.6,
                        1, 0, 0, 0, 0.1);
            }
            serverLevel.sendParticles(ParticleTypes.FLASH, x, y + attacker.getBbHeight() * 0.5, z, 1, 0, 0, 0, 0);
        }

        return Set.of();
    }

    @Override
    public @NonNull MoveType<LightningChargeMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull LightningChargeMove getThis() {
        return this;
    }

    @Override
    public @NonNull LightningChargeMove copy() {
        return copyExtras(new LightningChargeMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), chargeDurationTicks));
    }

    public static class Type extends AbstractMove.Type<LightningChargeMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<LightningChargeMove>, LightningChargeMove> buildCodec(RecordCodecBuilder.Instance<LightningChargeMove> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new LightningChargeMove(cd, wu, dur, md, 500));
        }
    }
}
