package net.arna.jcraft.common.attack.moves.mandom;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.BlockMarkerMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.MandomEntity;
import net.arna.jcraft.common.marker.*;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.TriConsumer;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public final class CountdownMove extends AbstractMove<CountdownMove, MandomEntity> implements BlockMarkerMove {
    private static final int COUNTDOWN_COOLDOWN_TICKS = 120; // 6 seconds
    // note that ReturnToZero move uses this same set as a default as well
    public static final Set<ResourceLocation> ENTITY_STUFF_TO_SAVE = Set.of(
            Identifiers.POSITION,
            Identifiers.YAW,
            Identifiers.YAW_HEAD,
            Identifiers.PITCH,
            Identifiers.VELOCITY,
            Identifiers.FALL_DISTANCE,
            Identifiers.FOOD_DATA,
            Identifiers.BLOOD_GAUGE,
            Identifiers.HAMON_CHARGE,
            Identifiers.HEALTH,
            Identifiers.AIR,
            Identifiers.ACTIVE_EFFECTS,
            Identifiers.VEHICLE
    );
    static final BlockMarkerType BLOCK_MARKER_TYPE = new BlockMarkerType(
            (pos, state) -> JServerConfig.MANDOM_AFFECTS_BLOCKS.getValue(),
            (marker, level) -> true
    );
    @Getter
    private final int radius;
    @Getter
    private final int maxCountdownTicks;
    @Getter
    private final List<EntityMarker> timeEntityMarkers = new LinkedList<>();
    @Getter
    private final List<BlockMarker> timeBlockMarkers = new ArrayList<>();
    @Getter
    @Setter
    private boolean resolving;
    @Getter
    private final EntityMarkerType entityMarkerType;
    @Getter
    private final List<RewindData> rewindInfo = new ArrayList<>();
    @Getter
    @Setter
    private boolean countdownActive = false;
    @Getter
    private int countdownTicks;
    private BlockPos attackerBlockPos;
    @Getter
    private final UUID uuid = UUID.randomUUID();
    @Getter
    private final List<Boolean> iteration = new LinkedList<>();
    private Level lastLevel = null;

    public CountdownMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int radius, final int maxCountdownTicks,
                         final @NonNull Set<ResourceLocation> rewindIds,
                         final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> extractor,
                         final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> injector) {
        super(cooldown, windup, duration, moveDistance);
        if (radius < 0) {
            throw new IllegalArgumentException("radius cannot be negative!");
        }
        this.radius = radius;
        if (maxCountdownTicks < 0) {
            throw new IllegalArgumentException("maxCountdownTicks cannot be negative!");
        }
        this.maxCountdownTicks = maxCountdownTicks;
        entityMarkerType = EntityMarkerType.defaultType(rewindIds, extractor, injector);
    }

    @Override
    public @NotNull MoveType<CountdownMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final MandomEntity attacker) {
        super.tick(attacker);
        if (++countdownTicks > maxCountdownTicks) {
            countdownActive = false;
        }
        if (!countdownActive) {
            countdownTicks = 0;
            return;
        }
        tickCountdownInfo(attacker);
    }

    @Override
    public boolean addBlock(final @NonNull BlockPos pos, final @NonNull BlockState state, final @NonNull Level level) {
        if (!countdownActive) {
            return false;
        }

        if (!isInRange(pos, level)) {
            return false;
        }

        if (resolving || !BLOCK_MARKER_TYPE.shouldSave(pos, state)) {
            return false;
        }
        // we only want to save the block if the pos isn't already marked
        // i.e. no point in saving that you replaced a block with dirt when it was previously already replaced,
        // e.g. with cobblestone
        for (final BlockMarker timeBlockMarker : timeBlockMarkers) {
            if (timeBlockMarker.pos().equals(pos)) {
                return false;
            }
        }
        timeBlockMarkers.add(BLOCK_MARKER_TYPE.save(pos, state));
        return true;
    }

    @Override
    public boolean removeBlock(@NonNull final BlockPos pos, @NonNull final Level level) {
        if (!countdownActive || !isInRange(pos, level) || resolving) {
            return false;
        }
        BlockMarker match = null;
        for (final BlockMarker timeBlockMarker : timeBlockMarkers) {
            if (timeBlockMarker.pos().equals(pos)) {
                match = timeBlockMarker;
                break;
            }
        }
        if (match != null) {
            timeBlockMarkers.remove(match);
        }
        return match != null;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final MandomEntity attacker, final LivingEntity user) {
        lastLevel = attacker.level();
        if (isRecording()) {
            getIteration().add(false);
        }
        BlockMarkerMoves.add(attacker, this);
        final List<Entity> toCapture = attacker.level().getEntitiesOfClass(Entity.class,
                attacker.getBoundingBox().inflate(radius),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != attacker));

        // Also include the user in the rewind
        if (!(user instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            toCapture.add(user);
        }

        timeEntityMarkers.clear();
        timeBlockMarkers.clear();
        rewindInfo.clear();

        attackerBlockPos = attacker.blockPosition();

        for (final Entity entity : toCapture) {
            if (entityMarkerType.shouldSave(entity.getUUID(), entity)) {
                timeEntityMarkers.add(entityMarkerType.save(entity.getUUID(), entity));
                rewindInfo.add(new RewindData(entity.getEyePosition(), entity));
            }
        }

        countdownActive = true;
        countdownTicks = 0;
        resolving = false;

        // Put both UTILITY and ULTIMATE on cooldown for 6 seconds
        CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
        cooldowns.setCooldown(CooldownType.UTILITY, COUNTDOWN_COOLDOWN_TICKS);
        cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, COUNTDOWN_COOLDOWN_TICKS);

        return Set.of();
    }


    public void tickCountdownInfo(final MandomEntity attacker) {
        if (!(attacker.getUser() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (rewindInfo.isEmpty()) {
            return;
        }

        for (RewindData data : rewindInfo) {
            final Entity entity = data.entity();
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            final Vec3 position = data.originalPos();

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            buf.writeInt(entity.getId());
            buf.writeDouble(position.x());
            buf.writeDouble(position.y());
            buf.writeDouble(position.z());
            final Vector3f color = attacker.getAuraColor();
            buf.writeFloat(color.x());
            buf.writeFloat(color.y());
            buf.writeFloat(color.z());

            NetworkManager.sendToPlayer(serverPlayer, JPacketRegistry.S2C_MANDOM_DATA, buf);
        }
    }

    @Override
    public boolean isInRange(final @NonNull BlockPos pos, final @NonNull Level level) {
        return level == lastLevel && pos.distSqr(attackerBlockPos) <= radius * radius;
    }

    @Override
    public boolean isRecording() {
        return countdownActive;
    }

    @Override
    protected @NonNull CountdownMove getThis() {
        return this;
    }

    @Override
    public @NonNull CountdownMove copy() {
        return copyExtras(new CountdownMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getRadius(), getMaxCountdownTicks(),
                entityMarkerType.getIds(), entityMarkerType.getDataHandler().extractor(), entityMarkerType.getDataHandler().injector()));
    }

    public static class Type extends AbstractMove.Type<CountdownMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<CountdownMove>, CountdownMove> buildCodec(RecordCodecBuilder.Instance<CountdownMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").forGetter(CountdownMove::getRadius), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("maxCountdownTicks").forGetter(CountdownMove::getMaxCountdownTicks), ResourceLocation.CODEC.listOf().xmap(list -> list.stream().collect(Collectors.toSet()), set -> set.stream().toList()).fieldOf("rewindIds").forGetter(move -> move.getEntityMarkerType().getIds()), JRegistries.EXTRACTOR_CODEC.fieldOf("extractor").forGetter(move -> move.getEntityMarkerType().getDataHandler().extractor()), JRegistries.INJECTOR_CODEC.fieldOf("injector").forGetter(move -> move.getEntityMarkerType().getDataHandler().injector())).apply(instance, applyExtras(CountdownMove::new));
        }
    }
}