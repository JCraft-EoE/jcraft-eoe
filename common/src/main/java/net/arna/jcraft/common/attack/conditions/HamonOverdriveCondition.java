package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.spec.HamonSpec;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonOverdriveCondition extends MoveCondition<HamonOverdriveCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonOverdriveCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonOverdriveCondition of(final int lessonTime) {
        return new HamonOverdriveCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        return HamonConditions.test(attacker.getUser(), 2, lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonOverdriveCondition> getType() {
        return HamonOverdriveCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonOverdriveCondition> {
        public static final HamonOverdriveCondition.Type INSTANCE = new HamonOverdriveCondition.Type();

        @Override
        public Codec<HamonOverdriveCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonOverdriveCondition::getLessonTime)
            ).apply(instance, HamonOverdriveCondition::new));
        }
    }
}
