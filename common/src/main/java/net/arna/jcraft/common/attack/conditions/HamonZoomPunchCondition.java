package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.common.advancements.Hamon3Trigger;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public class HamonZoomPunchCondition extends MoveCondition<HamonZoomPunchCondition, HamonSpec> {

    @Getter
    private final int lessonTime;

    private HamonZoomPunchCondition(final int lessonTime) {
        this.lessonTime = lessonTime;
    }

    public static HamonZoomPunchCondition of(final int lessonTime) {
        return new HamonZoomPunchCondition(lessonTime);
    }

    @Override
    public boolean test(final HamonSpec attacker) {
        if (!attacker.hasUser()) {
            return false;
        }
        final LivingEntity user = attacker.getUserOrThrow();
        final boolean hasCompletedTraining = !(user instanceof ServerPlayer player) || JUtils.hasAdvancement(player, Hamon3Trigger.ID);
        final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(user);
        return hasCompletedTraining || (hamon.getActiveLesson() == 3 && hamon.getLessonTicks(3) <= lessonTime);
    }

    @Override
    public @NotNull MoveConditionType<HamonZoomPunchCondition> getType() {
        return HamonZoomPunchCondition.Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<HamonZoomPunchCondition> {
        public static final HamonZoomPunchCondition.Type INSTANCE = new HamonZoomPunchCondition.Type();

        @Override
        public Codec<HamonZoomPunchCondition> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("lessonTime").forGetter(HamonZoomPunchCondition::getLessonTime)
            ).apply(instance, HamonZoomPunchCondition::new));
        }
    }
}
