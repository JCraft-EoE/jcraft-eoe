package net.arna.jcraft.common.attack.moves.vampire;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class NightVisionMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<NightVisionMove<A>, A> {
    private boolean active = false;

    public NightVisionMove(int cooldown) {
        super(cooldown, 0, 0, 0);
    }

    @Override
    public @NonNull MoveType<NightVisionMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        active = !active;
        if (!active) {
            user.removeEffect(MobEffects.NIGHT_VISION);
        }

        return Set.of();
    }

    @Override
    public void tick(final A attacker) {
        LivingEntity user = attacker.getUser();
        if (user == null || !active) return;

        user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 410, 0, true, false));
    }

    @Override
    protected @NonNull NightVisionMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull NightVisionMove<A> copy() {
        return copyExtras(new NightVisionMove<>(getCooldown()));
    }

    public static class Type extends AbstractMove.Type<NightVisionMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<NightVisionMove<?>>, NightVisionMove<?>> buildCodec(RecordCodecBuilder.Instance<NightVisionMove<?>> instance) {
            return instance.group(extras(), cooldown()).apply(instance, applyExtras(NightVisionMove::new));
        }
    }
}
