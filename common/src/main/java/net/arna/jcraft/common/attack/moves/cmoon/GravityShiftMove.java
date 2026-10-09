package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class GravityShiftMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<GravityShiftMove<A>, A> {
    public GravityShiftMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull MoveType<GravityShiftMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        JComponentPlatformUtils.getGravityShift(user).startRadial();
        return Set.of();
    }

    @Override
    protected @NonNull GravityShiftMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull GravityShiftMove<A> copy() {
        return copyExtras(new GravityShiftMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<GravityShiftMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<GravityShiftMove<?>>, GravityShiftMove<?>> buildCodec(
                final RecordCodecBuilder.Instance<GravityShiftMove<?>> instance) {
            return baseDefault(instance, GravityShiftMove::new);
        }
    }
}
