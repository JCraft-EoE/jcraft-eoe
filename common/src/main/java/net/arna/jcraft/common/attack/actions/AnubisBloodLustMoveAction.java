package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
public class AnubisBloodLustMoveAction extends MoveAction<AnubisBloodLustMoveAction, AnubisSpec> {
    private final boolean resetOnly;

    private AnubisBloodLustMoveAction(boolean resetOnly) {
        this.resetOnly = resetOnly;
    }

    public static AnubisBloodLustMoveAction incrementBloodlust() {
        return new AnubisBloodLustMoveAction(false);
    }

    public static AnubisBloodLustMoveAction resetLastHitTicks() {
        return new AnubisBloodLustMoveAction(true);
    }

    @Override
    public void perform(AnubisSpec attacker, LivingEntity user, Set<LivingEntity> targets) {
        if (resetOnly) attacker.setTicksSinceLastHit(0);
        else attacker.tryIncrementBloodlust(targets);
    }

    @Override
    public @NonNull MoveActionType<AnubisBloodLustMoveAction> getType() {
        return Type.INSTANCE;
    }

    public static class Type extends MoveActionType<AnubisBloodLustMoveAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<AnubisBloodLustMoveAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(), Codec.BOOL.fieldOf("reset_only").forGetter(AnubisBloodLustMoveAction::isResetOnly)
            ).apply(instance, apply(AnubisBloodLustMoveAction::new)));
        }
    }
}
