package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.spec.HamonSpec;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonChargeCondition extends MoveCondition<HamonChargeCondition, HamonSpec> {
    private final float requiredCharge;

    private HamonChargeCondition(float minCharge) {
        this.requiredCharge = minCharge;
    }

    public static HamonChargeCondition atLeast(float minCharge) {
        return new HamonChargeCondition(minCharge);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        return attacker.getCharge() >= requiredCharge;
    }

    @Override
    public @NotNull MoveConditionType<HamonChargeCondition> getType() {
        return HamonChargeCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonChargeCondition> {
        public static final HamonChargeCondition.Type INSTANCE = new HamonChargeCondition.Type();

        @Override
        public Codec<HamonChargeCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("min_charge").forGetter(HamonChargeCondition::getRequiredCharge)
            ).apply(instance, HamonChargeCondition::new));
        }
    }
}
