package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.marker.EntityMarker;
import net.arna.jcraft.common.marker.EntityMarkerType;
import net.arna.jcraft.common.util.CycleDeque;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.TriConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class ReturnToZeroHealMove extends AbstractMove<ReturnToZeroHealMove, GEREntity> {
    /**
     * How many stages there are.
     */
    @Getter
    private final int stepAmount;
    /**
     * How many ticks between two stages during return phase.
     */
    @Getter
    private final int stepDelay;
    /**
     * How many ticks between two stages being saved.
     */
    @Getter
    private final int stepSave;
    @Getter
    private final EntityMarkerType entityMarkerType;
    private final Deque<EntityMarker> returnEntityMarkers;
    private boolean started;
    private int ticksSinceLastStepSave;
    private int ticksSinceLastStepReturn;

    public ReturnToZeroHealMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int stepAmount, final int stepDelay, final int stepSave,
                                final @NonNull Set<ResourceLocation> rewindIds,
                                final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> extractor,
                                final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> injector) {
        super(cooldown, windup, duration, moveDistance);
        if (stepAmount < 0) {
            throw new IllegalArgumentException("RTZ heal step amount cannot be negative!");
        }
        this.stepAmount = stepAmount;
        returnEntityMarkers = new CycleDeque<>(stepAmount);
        if (stepDelay < 0) {
            throw new IllegalArgumentException("RTZ heal step delay cannot be negative!");
        }
        this.stepDelay = stepDelay;
        if (stepSave < 0) {
            throw new IllegalArgumentException("RTZ heal step save cannot be negative!");
        }
        this.stepSave = stepSave;
        entityMarkerType = EntityMarkerType.defaultType(rewindIds, extractor, injector);
    }

    @Override
    public @NotNull MoveType<ReturnToZeroHealMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final GEREntity attacker) {
        ticksSinceLastStepSave++;
        ticksSinceLastStepReturn++;
        if (started && ticksSinceLastStepReturn >= stepDelay) {
            returnStep(attacker);
        }
        else if (!started && ticksSinceLastStepSave >= stepSave) {
            saveStep(attacker);
        }
    }

    private void saveStep(final GEREntity attacker) {
        if (!attacker.hasUser()) {
            return;
        }
        final LivingEntity user = attacker.getUser();
        if (entityMarkerType.shouldSave(user.getUUID(), user)) {
            returnEntityMarkers.add(entityMarkerType.save(user.getUUID(), user));
        }
        ticksSinceLastStepSave = 0;
    }

    private void returnStep(final GEREntity attacker) {
        final EntityMarker marker = returnEntityMarkers.pollLast();
        if (!attacker.hasUser() || marker == null) {
            return;
        }
        final LivingEntity user = attacker.getUser();
        if (entityMarkerType.shouldLoad(marker, (ServerLevel)user.level())) {
            entityMarkerType.load(marker, (ServerLevel)user.level());
        }
        ticksSinceLastStepReturn = 0;
        if (returnEntityMarkers.isEmpty()) {
            started = false;
        }
        else {
            final Optional<Vec3> nextPosOp = returnEntityMarkers.peekLast().extractPosition();
            if (nextPosOp.isPresent()) {
                final Vec3 nextPos = nextPosOp.get();
                JUtils.setVelocity(user, nextPos.subtract(user.position()).scale(1.0 / (double)stepDelay));
            }
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final GEREntity attacker, final LivingEntity user) {
        started = true;
        returnStep(attacker);
        return Set.of();
    }

    @Override
    protected @NonNull ReturnToZeroHealMove getThis() {
        return this;
    }

    @Override
    public @NonNull ReturnToZeroHealMove copy() {
        return copyExtras(new ReturnToZeroHealMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStepAmount(), getStepDelay(), getStepSave(),
                entityMarkerType.getIds(), entityMarkerType.getDataHandler().extractor(), entityMarkerType.getDataHandler().injector()));
    }

    public static class Type extends AbstractMove.Type<ReturnToZeroHealMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<ReturnToZeroHealMove>, ReturnToZeroHealMove> buildCodec(RecordCodecBuilder.Instance<ReturnToZeroHealMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("step_amount").forGetter(ReturnToZeroHealMove::getStepAmount), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("step_delay").forGetter(ReturnToZeroHealMove::getStepDelay), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("step_save").forGetter(ReturnToZeroHealMove::getStepSave), ResourceLocation.CODEC.listOf().xmap(list -> list.stream().collect(Collectors.toSet()), set -> set.stream().toList()).fieldOf("rewindIds").forGetter(move -> move.getEntityMarkerType().getIds()), JRegistries.EXTRACTOR_CODEC.fieldOf("extractor").forGetter(move -> move.getEntityMarkerType().getDataHandler().extractor()), JRegistries.INJECTOR_CODEC.fieldOf("injector").forGetter(move -> move.getEntityMarkerType().getDataHandler().injector())).apply(instance, applyExtras(ReturnToZeroHealMove::new));
        }
    }
}
