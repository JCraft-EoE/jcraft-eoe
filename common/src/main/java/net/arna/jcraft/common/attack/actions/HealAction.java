package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
@RequiredArgsConstructor(staticName = "heal")
public class HealAction extends MoveAction<HealAction, IAttacker<?, ?>> {
    private final float amount;

    @Override
    public void perform(IAttacker<?, ?> attacker, LivingEntity user, Set<LivingEntity> targets) {
        for (LivingEntity target : targets) {
            target.heal(amount);
        }
    }

    @Override
    public @NonNull MoveActionType<HealAction> getType() {
        return Type.INSTANCE;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Type extends MoveActionType<HealAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<HealAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    Codec.FLOAT.fieldOf("amount").forGetter(HealAction::getAmount)
            ).apply(instance, apply((duration) -> HealAction.heal(duration))));
        }
    }
}
