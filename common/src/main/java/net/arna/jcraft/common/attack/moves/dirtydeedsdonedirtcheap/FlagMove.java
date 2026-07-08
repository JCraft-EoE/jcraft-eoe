package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public final class FlagMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<FlagMove<A>, A> {
    public FlagMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.HIGHJUMP;
    }

    @Override
    public @NonNull MoveType<FlagMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        attacker.getUserOrThrow().addEffect(
                new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), getDuration(), 0, true, false)
        );
        attacker.getUserOrThrow().addEffect(
                new MobEffectInstance(MobEffects.SLOW_FALLING, getDuration(), 0, true, false)
        );
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        int duration = getWindupPoint();
        user.addEffect(
                new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, true, false)
        );
        user.addEffect(
                new MobEffectInstance(MobEffects.LEVITATION, duration, 2, true, false)
        );

        return Set.of();
    }

    @Override
    protected @NonNull FlagMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull FlagMove<A> copy() {
        return copyExtras(new FlagMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<FlagMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlagMove<?>>, FlagMove<?>> buildCodec(RecordCodecBuilder.Instance<FlagMove<?>> instance) {
            return baseDefault(instance, FlagMove::new);
        }
    }
}
