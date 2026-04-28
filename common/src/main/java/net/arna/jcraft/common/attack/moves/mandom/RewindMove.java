package net.arna.jcraft.common.attack.moves.mandom;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.MandomEntity;
import net.arna.jcraft.common.marker.BlockMarker;
import net.arna.jcraft.common.marker.EntityMarker;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class RewindMove extends AbstractMove<RewindMove, MandomEntity> {

    @Getter
    private final int reach; // in Euclidean distance in meters

    public RewindMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int reach) {
        super(cooldown, windup, duration, moveDistance);
        if (reach < 0) {
            throw new IllegalArgumentException("Rewind teleport reach cannot be negative!");
        }
        this.reach = reach;
    }

    @Override
    public @NotNull MoveType<RewindMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final MandomEntity attacker, final LivingEntity user) {
        CountdownMove countdownMove = attacker.getMove(CountdownMove.class);
        if (countdownMove == null || !countdownMove.isCountdownActive()) {
            return Set.of();
        }

        final ServerLevel level = (ServerLevel) attacker.level();

        if (user instanceof ServerPlayer serverPlayer) {
            ShaderActivationPacket.sendMandomRewind(serverPlayer, 23, attacker.getAuraColor());
        }

        final List<BlockMarker> blockMarkers = countdownMove.getTimeBlockMarkers();
        countdownMove.setResolving(true);
        for (final BlockMarker marker : blockMarkers) {
            if (CountdownMove.BLOCK_MARKER_TYPE.shouldLoad(marker, level)) {
                CountdownMove.BLOCK_MARKER_TYPE.load(marker, level);
            }
        }

        final List<EntityMarker> entityMarkers = countdownMove.getTimeEntityMarkers();

        for (final EntityMarker marker : entityMarkers) {
            if (countdownMove.getEntityMarkerType().shouldLoad(marker, level) && JUtils.nullSafeDistanceSqr(level.getEntity(marker.id()), attacker.getUser()) <= reach * reach) {
                countdownMove.getEntityMarkerType().load(marker, level);
            }
        }

        // Clean up
        countdownMove.getIteration().add(true);
        entityMarkers.clear();
        blockMarkers.clear();
        countdownMove.getRewindInfo().clear();
        countdownMove.setCountdownActive(false);

        return Set.of();
    }

    @Override
    protected @NonNull RewindMove getThis() {
        return this;
    }

    @Override
    public @NonNull RewindMove copy() {
        return copyExtras(new RewindMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), 200));
    }

    public static final class Type extends AbstractMove.Type<RewindMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<RewindMove>, RewindMove> buildCodec(RecordCodecBuilder.Instance<RewindMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("reach").forGetter(RewindMove::getReach)).apply(instance, applyExtras(RewindMove::new));
        }
    }
}