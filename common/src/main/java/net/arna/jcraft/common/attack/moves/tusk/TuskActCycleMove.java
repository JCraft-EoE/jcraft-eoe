package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Tusk Act Cycle Move
 * Click Utility: Cycle up through acts (1→2→3→1...)
 * Shift + Utility: Cycle down through acts (3→2→1→3...)
 * Only cycles through unlocked acts (based on CommonMiscComponent.getHighestTuskAct())
 */
public final class TuskActCycleMove<A extends IAttacker<A, ?>> extends AbstractMove<TuskActCycleMove<A>, A> {
    @Getter
    private final int currentAct;
    @Getter
    private final boolean backward;

    public TuskActCycleMove(int cooldown, int windup, int duration, float moveDistance, int currentAct, boolean backward) {
        super(cooldown, windup, duration, moveDistance);
        this.currentAct = currentAct;
        this.backward = backward;
    }

    @Override
    public @NotNull MoveType<TuskActCycleMove<A>> getMoveType() {
        return (MoveType<TuskActCycleMove<A>>) (MoveType<?>) Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (!(user instanceof Player player)) return Set.of();
        if (player.level().isClientSide()) return Set.of();

        CommonMiscComponent miscComponent = JComponentPlatformUtils.getMiscData(player);
        if (miscComponent == null) return Set.of();

        int maxUnlockedAct = miscComponent.getHighestTuskAct();
        if (maxUnlockedAct < 1) maxUnlockedAct = 1;

        int nextAct = getNextAct(currentAct, maxUnlockedAct, backward);
        if (nextAct == currentAct) return Set.of(); // Only one act unlocked

        CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(player);
        StandType newType = getStandTypeForAct(nextAct);
        if (newType == null) return Set.of();

        StandEntity<?, ?> currentStand = standData.getStand();
        if (currentStand != null) {
            currentStand.desummon(false);
        }

        standData.setType(newType);
        JCraft.summon(player.level(), player);

        return Set.of();
    }

    private int getNextAct(int current, int maxUnlocked, boolean backward) {
        if (maxUnlocked <= 1) return current; // Nothing to cycle to
        if (backward) {
            int next = current - 1;
            if (next < 1) next = maxUnlocked;
            return next;
        } else {
            int next = current + 1;
            if (next > maxUnlocked) next = 1;
            return next;
        }
    }

    private StandType getStandTypeForAct(int act) {
        return switch (act) {
            case 1 -> JStandTypeRegistry.TUSK_ACT_1.get();
            case 2 -> JStandTypeRegistry.TUSK_ACT_2.get();
            case 3 -> JStandTypeRegistry.TUSK_ACT_3.get();
            default -> null;
        };
    }

    @Override
    protected @NonNull TuskActCycleMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TuskActCycleMove<A> copy() {
        return copyExtras(new TuskActCycleMove<>(
                getCooldown(), getWindup(), getDuration(), getMoveDistance(), currentAct, backward
        ));
    }

    public static class Type extends AbstractMove.Type<TuskActCycleMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<TuskActCycleMove<?>>, TuskActCycleMove<?>>
        buildCodec(RecordCodecBuilder.Instance<TuskActCycleMove<?>> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.INT.fieldOf("current_act").forGetter(TuskActCycleMove::getCurrentAct),
                    Codec.BOOL.fieldOf("backward").forGetter(TuskActCycleMove::isBackward)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, currentAct, backward) ->
                    new TuskActCycleMove<>(cooldown, windup, duration, moveDistance, currentAct, backward)
            ));
        }
    }
}
