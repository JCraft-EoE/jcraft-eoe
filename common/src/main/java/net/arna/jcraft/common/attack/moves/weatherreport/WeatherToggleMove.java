package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public final class WeatherToggleMove extends AbstractMove<WeatherToggleMove, WeatherReportEntity> {

    private static final int STATE_COUNT = 3;
    private static final String[] STATE_NAMES = {"Clear", "Rain", "Thunder"};

    public WeatherToggleMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return Set.of();

        final int currentState = getCurrentWeatherState(serverLevel);
        final int nextState = (currentState + 1) % STATE_COUNT;

        applyWeatherState(serverLevel, nextState);

        if (user instanceof Player player) {
            player.displayClientMessage(Component.literal("Weather: " + STATE_NAMES[nextState]), true);
        }
        return Set.of();
    }

    private int getCurrentWeatherState(final ServerLevel level) {
        if (level.isThundering()) return 2;
        if (level.isRaining()) return 1;
        return 0;
    }

    private void applyWeatherState(final ServerLevel level, final int state) {
        switch (state) {
            case 0 -> level.setWeatherParameters(6000, 0, false, false);
            case 1 -> level.setWeatherParameters(0, 6000, true, false);
            case 2 -> level.setWeatherParameters(0, 6000, true, true);
        }
    }

    @Override
    public @NonNull MoveType<WeatherToggleMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull WeatherToggleMove getThis() {
        return this;
    }

    @Override
    public @NonNull WeatherToggleMove copy() {
        return copyExtras(new WeatherToggleMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<WeatherToggleMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<WeatherToggleMove>, WeatherToggleMove> buildCodec(RecordCodecBuilder.Instance<WeatherToggleMove> instance) {
            return baseDefault(instance, WeatherToggleMove::new);
        }
    }
}
