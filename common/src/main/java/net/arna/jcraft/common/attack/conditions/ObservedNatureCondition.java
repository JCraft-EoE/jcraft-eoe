package net.arna.jcraft.common.attack.conditions;

import com.mojang.serialization.Codec;
import lombok.NonNull;
import net.arna.jcraft.api.attack.core.MoveCondition;
import net.arna.jcraft.api.attack.core.MoveConditionType;
import net.arna.jcraft.common.advancements.ObservedNatureTrigger;
import net.arna.jcraft.common.spec.SpinSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class ObservedNatureCondition extends MoveCondition<ObservedNatureCondition, SpinSpec> {

    public static final ObservedNatureCondition INSTANCE = new ObservedNatureCondition();

    private ObservedNatureCondition() {
        // singleton
    }

    @Override
    public boolean test(final SpinSpec attacker) {
        if (attacker == null || attacker.getUser() == null) {
            return false;
        }
        final LivingEntity user = attacker.getUser();
        if (user instanceof final ServerPlayer player) {
            return JUtils.hasAdvancement(player, ObservedNatureTrigger.ID);
        }
        return true;
    }

    @Override
    public @NonNull MoveConditionType<ObservedNatureCondition> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements MoveConditionType<ObservedNatureCondition> {

        public static final Type INSTANCE = new Type();

        @Override
        public Codec<ObservedNatureCondition> getCodec() {
            return Codec.unit(ObservedNatureCondition::new);
        }

    }

}
