package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

import java.util.Set;

/**
 * An action solely for the purpose of hooking the hamon stomp move into the hamon component.
 */
@RequiredArgsConstructor(staticName = "run")
public class NotifyHamonStompAction extends MoveAction<NotifyHamonStompAction, IAttacker<?,?>> {

    @Override
    public void perform(final IAttacker<?, ?> attacker, final LivingEntity user, final Set<LivingEntity> targets) {
        if (user == null || targets == null || targets.isEmpty()) {
            return;
        }
        final JSpec<?,?> userSpec = JUtils.getSpec(user);
        if (userSpec != null && userSpec.getType() == JSpecTypeRegistry.HAMON.get()) {
            final var hamon = JComponentPlatformUtils.getHamon(user);
            for (final LivingEntity target : targets) {
                if (target instanceof Enemy) {
                    hamon.setLastStomped(target.getUUID());
                }
            }
        }
    }

    @Override
    public @NonNull MoveActionType<NotifyHamonStompAction> getType() {
        return Type.INSTANCE;
    }


    public static class Type extends MoveActionType<NotifyHamonStompAction> {
        public static final Type INSTANCE = new NotifyHamonStompAction.Type();

        @Override
        public Codec<NotifyHamonStompAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment()).apply(instance, apply(NotifyHamonStompAction::run)));
        }
    }
}
