package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "play")
public class UserAnimationAction extends MoveAction<UserAnimationAction, IAttacker<?, ?>> {
    private final String animation;
    private boolean force = false; // not final, but it is immutable. // why lol

    public UserAnimationAction force() {
        return new UserAnimationAction(animation, true);
    }

    @Override
    public void perform(IAttacker<?, ?> attacker, LivingEntity user, Set<LivingEntity> targets) {
        if (force) JUtils.playAnim(user, animation);
        else JUtils.playAnimIfUnoccupied(user, animation);
    }

    @Override
    public @NonNull MoveActionType<UserAnimationAction> getType() {
        return Type.INSTANCE;
    }

    public static class Type extends MoveActionType<UserAnimationAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<UserAnimationAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    Codec.STRING.fieldOf("animation").forGetter(UserAnimationAction::getAnimation),
                    Codec.BOOL.optionalFieldOf("force", false).forGetter(UserAnimationAction::isForce)
            ).apply(instance, apply((anim, force) -> new UserAnimationAction(anim, force))));
        }
    }
}
