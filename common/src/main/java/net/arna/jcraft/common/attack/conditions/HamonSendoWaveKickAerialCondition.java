package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.spec.HamonSpec;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonSendoWaveKickAerialCondition extends MoveCondition<HamonSendoWaveKickAerialCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonSendoWaveKickAerialCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonSendoWaveKickAerialCondition of(final int lessonTime) {
        return new HamonSendoWaveKickAerialCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        return HamonConditions.test(attacker.getUser(), 6, lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonSendoWaveKickAerialCondition> getType() {
        return HamonSendoWaveKickAerialCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonSendoWaveKickAerialCondition> {
        public static final HamonSendoWaveKickAerialCondition.Type INSTANCE = new HamonSendoWaveKickAerialCondition.Type();

        @Override
        public Codec<HamonSendoWaveKickAerialCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonSendoWaveKickAerialCondition::getLessonTime)
            ).apply(instance, HamonSendoWaveKickAerialCondition::new));
        }
    }
}
