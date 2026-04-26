package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static net.arna.jcraft.common.attack.moves.weatherreport.WindSpeed.values;

import java.util.Set;

public final class WindTunnelSpeedSelectMove extends AbstractMove<WindTunnelSpeedSelectMove, WeatherReportEntity> {

    public WindTunnelSpeedSelectMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final int next = (attacker.getWindSpeedIndex() + 1) % values().length;
        attacker.setWindSpeedIndex(next);
        if (user instanceof Player player) {
            player.displayClientMessage(Component.literal("Wind Speed: " + values()[next].displayName()), true);
        }
        return Set.of();
    }

    @Override
    public @NonNull MoveType<WindTunnelSpeedSelectMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WindTunnelSpeedSelectMove getThis() {
        return this;
    }

    @Override
    public @NonNull WindTunnelSpeedSelectMove copy() {
        return copyExtras(new WindTunnelSpeedSelectMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<WindTunnelSpeedSelectMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WindTunnelSpeedSelectMove>, WindTunnelSpeedSelectMove> buildCodec(RecordCodecBuilder.Instance<WindTunnelSpeedSelectMove> instance) {
            return baseDefault(instance, WindTunnelSpeedSelectMove::new);
        }
    }
}
