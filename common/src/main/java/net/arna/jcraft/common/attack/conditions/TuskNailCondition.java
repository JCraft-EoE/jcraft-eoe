package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.api.stand.StandEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public class TuskNailCondition extends MoveCondition<TuskNailCondition, StandEntity<?, ?>> {
    private final float requiredNails;

    private TuskNailCondition(float minNails) {
        this.requiredNails = minNails;
    }

    public static TuskNailCondition atLeast(float minNails) {
        return new TuskNailCondition(minNails);
    }

    @Override
    public boolean test(final StandEntity<?, ?> attacker) {
        // This will be overridden in each Tusk entity with getNails() method
        if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct1Entity tusk1) {
            return tusk1.getNails() >= requiredNails;
        } else if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct2Entity tusk2) {
            return tusk2.getNails() >= requiredNails;
        } else if (attacker instanceof net.arna.jcraft.common.entity.stand.TuskAct3Entity tusk3) {
            return tusk3.getNails() >= requiredNails;
        }
        return false;
    }

    @Override
    public @NotNull MoveConditionType<TuskNailCondition> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<TuskNailCondition> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<TuskNailCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("min_nails").forGetter(TuskNailCondition::getRequiredNails)
            ).apply(instance, TuskNailCondition::new));
        }
    }
}