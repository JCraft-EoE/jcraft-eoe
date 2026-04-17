package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.common.advancements.Hamon6Trigger;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
        if (!attacker.hasUser()) {
            return false;
        }
        final LivingEntity user = attacker.getUserOrThrow();
        final boolean hasCompletedTraining = !(user instanceof ServerPlayer player) || JUtils.hasAdvancement(player, Hamon6Trigger.ID);
        final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(user);
        return hasCompletedTraining || (hamon.getActiveLesson() == 6 && hamon.getLessonTicks(6) <= lessonTime);
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
