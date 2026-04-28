package net.arna.jcraft.common.attack.moves.hamon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.spec.HamonSpec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class ImproviserMove extends AbstractBarrageAttack<ImproviserMove, HamonSpec> {

    @Getter
    private final float chargePerTick;

    public ImproviserMove(final int duration, final float chargePerTick, final int interval) {
        super(0, 0, duration, 0f, 0, 0, 0f, 0f, 0f, interval);
        this.chargePerTick = chargePerTick;
        withHoldable();
        withoutSlowness();
    }

    @Override
    public void onUserMoveInput(final HamonSpec attacker, final MoveInputType type, final boolean pressed, final  boolean moveInitiated) {
        super.onUserMoveInput(attacker, type, pressed, moveInitiated);
        // Must be held
        if (type.getMoveClass() == getMoveClass() && !pressed) {
            attacker.cancelMove();
            attacker.updateClientHamonBar();
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final HamonSpec attacker, final LivingEntity user) {
        if (attacker.hasUser() && attacker.getCharge() >= chargePerTick) {
            final LivingEntity living = attacker.getUserOrThrow();
            if (!(living instanceof ServerPlayer player) || !player.isCreative()) {
                attacker.drainCharge(chargePerTick);
            }
            living.addEffect(new MobEffectInstance(JStatusRegistry.WATER_WALKING.get(), 1, 0, false, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1, 0, false, false, true));
        }
        return Set.of();
    }

    @Override
    protected @NonNull ImproviserMove getThis() {
        return this;
    }

    @Override
    public @NonNull ImproviserMove copy() {
        return copyExtras(new ImproviserMove(getDuration(), getChargePerTick(), getInterval()));
    }

    @Override
    public @NonNull MoveType<ImproviserMove> getMoveType() {
        return Type.INSTANCE.cast();
    }

    public static class Type extends AbstractBarrageAttack.Type<ImproviserMove> {

        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<ImproviserMove, Float> chargePerTick() {
            return Codec.FLOAT.fieldOf("chargePerTick").forGetter(ImproviserMove::getChargePerTick);
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ImproviserMove>, ImproviserMove> buildCodec(final RecordCodecBuilder.Instance<ImproviserMove> instance) {
            return instance.group(extras(), attackExtras(), duration(), chargePerTick(), interval()).apply(instance, applyAttackExtras(ImproviserMove::new));
        }

    }

}
