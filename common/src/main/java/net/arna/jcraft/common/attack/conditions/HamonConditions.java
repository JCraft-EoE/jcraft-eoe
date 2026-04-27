package net.arna.jcraft.common.attack.conditions;

import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.common.advancements.*;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class HamonConditions {
    private static final ResourceLocation[] HAMON_TRIGGERS = {
            Hamon1Trigger.ID,
            Hamon2Trigger.ID,
            Hamon3Trigger.ID,
            Hamon4Trigger.ID,
            Hamon5Trigger.ID,
            Hamon6Trigger.ID,
    };

    public static boolean test(@Nullable final LivingEntity user, @Range(from = 1, to = 6) final int lessonId, final int lessonTime) {
        if (user == null) return false;

        if (user instanceof ServerPlayer player && JUtils.isMortal(player)) {
            final boolean hasCompletedTraining = JUtils.hasAdvancement(player, HAMON_TRIGGERS[lessonId - 1]);
            final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(user);
            return hasCompletedTraining || (hamon.getActiveLesson() == lessonId && hamon.getLessonTicks(lessonId) <= lessonTime);
        }

        return true;
    }

    public static boolean test(@Nullable final LivingEntity user, final int lessonId) {
        return test(user, lessonId, 1800);
    }
}
