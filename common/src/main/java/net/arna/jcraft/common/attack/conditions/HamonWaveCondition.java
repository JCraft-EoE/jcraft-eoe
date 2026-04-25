package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.spec.HamonSpec;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonWaveCondition extends MoveCondition<HamonWaveCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonWaveCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonWaveCondition of(final int lessonTime) {
        return new HamonWaveCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        return HamonConditions.test(attacker.getUser(), 5, lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonWaveCondition> getType() {
        return HamonWaveCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonWaveCondition> {
        public static final HamonWaveCondition.Type INSTANCE = new HamonWaveCondition.Type();

        @Override
        public Codec<HamonWaveCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonWaveCondition::getLessonTime)
            ).apply(instance, HamonWaveCondition::new));
        }
    }
}
