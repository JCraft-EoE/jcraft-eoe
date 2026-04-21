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
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Utility move that cycles through unlocked Tusk acts (forward or backward).
 * Switching is handled directly in each entity's initMove() via tryCycle().
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
        return Set.of(); // Switching is handled in initMove directly; this is never reached
    }

    private static boolean cycling = false;

    /** Switches to the next (or previous, if shift is held) unlocked Tusk act. Mirrors /stand set. */
    public static boolean tryCycle(int currentAct, @Nullable LivingEntity user) {
        if (cycling) return false;
        cycling = true;
        try {
            if (!(user instanceof ServerPlayer sp)) return false;
            if (sp.level().isClientSide()) return false;

            int maxAct = getMaxActFromAdvancements(sp);
            if (maxAct <= 1) return false; // Nothing to cycle to

            int nextAct;
            if (sp.isShiftKeyDown()) {
                nextAct = currentAct - 1;
                if (nextAct < 1) nextAct = maxAct;
            } else {
                nextAct = currentAct + 1;
                if (nextAct > maxAct) nextAct = 1;
            }

            if (nextAct == currentAct) return false;

            StandType newType = getActStandType(nextAct);
            if (newType == null) return false;

            CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(sp);
            standData.setTypeAndSkin(newType, 0);
            JUtils.maySendStandAboutInfo(sp);
            Entity vehicle = sp.getVehicle();
            sp.unRide();
            JCraft.summon(sp.level(), sp);
            if (vehicle != null && vehicle.isAlive()) sp.startRiding(vehicle, true);
            return true;
        } finally {
            cycling = false;
        }
    }

    public static @Nullable StandType getActStandType(int act) {
        return switch (act) {
            case 1 -> JStandTypeRegistry.TUSK_ACT_1.get();
            case 2 -> JStandTypeRegistry.TUSK_ACT_2.get();
            case 3 -> JStandTypeRegistry.TUSK_ACT_3.get();
            default -> null;
        };
    }

    /** Returns the highest act the player has unlocked, determined from advancements. */
    private static int getMaxActFromAdvancements(ServerPlayer player) {
        var serverAdv = player.getServer() != null ? player.getServer().getAdvancements() : null;
        if (serverAdv == null) return 1;
        int max = 1;
        Advancement act2 = serverAdv.getAdvancement(new ResourceLocation("jcraft", "tusk_act2"));
        if (act2 != null && player.getAdvancements().getOrStartProgress(act2).isDone()) max = 2;
        Advancement act3 = serverAdv.getAdvancement(new ResourceLocation("jcraft", "tusk_act3"));
        if (act3 != null && player.getAdvancements().getOrStartProgress(act3).isDone()) max = 3;
        return max;
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
