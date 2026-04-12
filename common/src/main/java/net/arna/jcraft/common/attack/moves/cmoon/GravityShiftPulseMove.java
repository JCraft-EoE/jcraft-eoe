package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class GravityShiftPulseMove extends AbstractMove<GravityShiftPulseMove, CMoonEntity> {
    @Getter
    private final int radius;

    public GravityShiftPulseMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int radius) {
        super(cooldown, windup, duration, moveDistance);
        this.radius = radius;
    }

    @Override
    public @NotNull MoveType<GravityShiftPulseMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final CMoonEntity attacker, final LivingEntity user) {
        JComponentPlatformUtils.getGravityShift(user).startDirectional(radius);
        return Set.of();
    }

    @Override
    protected @NonNull GravityShiftPulseMove getThis() {
        return this;
    }

    @Override
    public @NonNull GravityShiftPulseMove copy() {
        return copyExtras(new GravityShiftPulseMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), 16));
    }

    public static class Type extends AbstractMove.Type<GravityShiftPulseMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<GravityShiftPulseMove>, GravityShiftPulseMove> buildCodec(RecordCodecBuilder.Instance<GravityShiftPulseMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").forGetter(GravityShiftPulseMove::getRadius)).apply(instance, applyExtras(GravityShiftPulseMove::new));
        }
    }
}
