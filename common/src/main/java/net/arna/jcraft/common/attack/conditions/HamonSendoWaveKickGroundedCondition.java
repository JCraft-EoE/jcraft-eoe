package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.common.advancements.Hamon4Trigger;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonSendoWaveKickGroundedCondition extends MoveCondition<HamonSendoWaveKickGroundedCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonSendoWaveKickGroundedCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonSendoWaveKickGroundedCondition of(final int lessonTime) {
        return new HamonSendoWaveKickGroundedCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        if (!attacker.hasUser()) {
            return false;
        }
        final LivingEntity user = attacker.getUserOrThrow();
        final boolean hasCompletedTraining = !(user instanceof ServerPlayer player) || JUtils.hasAdvancement(player, Hamon4Trigger.ID);
        final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(user);
        return hasCompletedTraining || (hamon.getActiveLesson() == 4 && hamon.getLessonTicks(4) <= lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonSendoWaveKickGroundedCondition> getType() {
        return HamonSendoWaveKickGroundedCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonSendoWaveKickGroundedCondition> {
        public static final HamonSendoWaveKickGroundedCondition.Type INSTANCE = new HamonSendoWaveKickGroundedCondition.Type();

        @Override
        public Codec<HamonSendoWaveKickGroundedCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonSendoWaveKickGroundedCondition::getLessonTime)
            ).apply(instance, HamonSendoWaveKickGroundedCondition::new));
        }
    }
}
