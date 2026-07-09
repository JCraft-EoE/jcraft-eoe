package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;

public class RemoteCondition extends MoveCondition<RemoteCondition, IAttacker<?, ?>> {
    @Getter
    private final boolean requireRemote;

    public RemoteCondition(final boolean requireRemote) {
        this.requireRemote = requireRemote;
    }

    @Override
    public boolean test(IAttacker<?, ?> attacker) {
        return attacker.isRemote() == requireRemote;
    }

    @Override
    public @NonNull MoveConditionType<RemoteCondition> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<RemoteCondition> {
        public static final RemoteCondition.Type INSTANCE = new RemoteCondition.Type();

        @Override
        public Codec<RemoteCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.BOOL.fieldOf("requireRemote").forGetter(RemoteCondition::isRequireRemote)
            ).apply(instance, RemoteCondition::new));
        }
    }
}
