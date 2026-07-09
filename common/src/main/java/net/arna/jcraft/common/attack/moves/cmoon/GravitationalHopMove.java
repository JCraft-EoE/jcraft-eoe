package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public final class GravitationalHopMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<GravitationalHopMove<A>, A> {
    private final int weightlessDuration, slowFallingDuration;

    public GravitationalHopMove(final int cooldown, final int weightlessDuration, final int slowFallingDuration) {
        super(cooldown, 0, 0, 0f);
        this.weightlessDuration = weightlessDuration;
        this.slowFallingDuration = slowFallingDuration;
        mobilityType = MobilityType.HIGHJUMP;
    }

    @Override
    public @NotNull MoveType<GravitationalHopMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (user.onGround()) {
            if (user.hasEffect(JStatusRegistry.WEIGHTLESS.get())) {
                user.removeEffect(JStatusRegistry.WEIGHTLESS.get());
            }
            user.addEffect(new MobEffectInstance(JStatusRegistry.WEIGHTLESS.get(), getWeightlessDuration(), 1));
        } else {
            user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, getSlowFallingDuration(), 1));
            user.push(0, 1.0, 0);
        }

        user.hurtMarked = true;
        return Set.of();
    }

    @Override
    protected @NonNull GravitationalHopMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull GravitationalHopMove<A> copy() {
        return copyExtras(new GravitationalHopMove<>(getCooldown(), getWeightlessDuration(), getSlowFallingDuration()));
    }

    public static class Type extends AbstractMove.Type<GravitationalHopMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<GravitationalHopMove<?>>, GravitationalHopMove<?>> buildCodec(RecordCodecBuilder.Instance<GravitationalHopMove<?>> instance) {
            return instance.group(extras(), cooldown(),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weightless_duration").forGetter(GravitationalHopMove::getWeightlessDuration),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("slow_falling_duration").forGetter(GravitationalHopMove::getSlowFallingDuration))
                    .apply(instance, applyExtras(GravitationalHopMove::new));
        }
    }
}
