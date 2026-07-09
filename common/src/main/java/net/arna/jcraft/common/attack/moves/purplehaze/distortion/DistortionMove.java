package net.arna.jcraft.common.attack.moves.purplehaze.distortion;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class DistortionMove<A extends AbstractPurpleHazeEntity<? extends A, ?>> extends AbstractMove<DistortionMove<A>, A> {
    public DistortionMove(int cooldown) {
        super(cooldown, 0, 0, 0);
    }

    @Override
    public @NonNull MoveType<DistortionMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        attacker.nextPoisonType();
        return Set.of();
    }

    @Override
    protected @NonNull DistortionMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull DistortionMove<A> copy() {
        return copyExtras(new DistortionMove<>(getCooldown()));
    }

    public static class Type extends AbstractMove.Type<DistortionMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<DistortionMove<?>>, DistortionMove<?>> buildCodec(RecordCodecBuilder.Instance<DistortionMove<?>> instance) {
            return instance.group(extras(), cooldown()).apply(instance, applyExtras(DistortionMove::new));
        }
    }
}
