package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.spec.HamonSpec;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonBreathCondition extends MoveCondition<HamonBreathCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonBreathCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonBreathCondition of(final int lessonTime) {
        return new HamonBreathCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        return HamonConditions.test(attacker.getUser(), 1, lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonBreathCondition> getType() {
        return HamonBreathCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonBreathCondition> {
        public static final HamonBreathCondition.Type INSTANCE = new HamonBreathCondition.Type();

        @Override
        public Codec<HamonBreathCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonBreathCondition::getLessonTime)
            ).apply(instance, HamonBreathCondition::new));
        }
    }
}
