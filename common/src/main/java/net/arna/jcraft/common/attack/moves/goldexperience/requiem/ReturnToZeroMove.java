package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.marker.*;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.TriConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class ReturnToZeroMove extends AbstractMove<ReturnToZeroMove, GEREntity> {
    @Getter
    private final int radius;
    @Getter
    private final int reach;
    @Getter
    private final EntityMarkerType entityMarkerType;
    private final List<EntityMarker> returnEntityMarkers = new LinkedList<>();
    @Getter
    private final List<RewindData> returnInfo = new ArrayList<>();
    private boolean started;

    public ReturnToZeroMove(final int cooldown, final int windup, final int duration, final float moveDistance, final int radius, final int reach,
                            final @NonNull Set<ResourceLocation> rewindIds,
                            final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> extractor,
                            final @NonNull TriConsumer<ResourceLocation,Entity,CompoundTag> injector) {
        super(cooldown, windup, duration, moveDistance);
        if (radius < 0) {
            throw new IllegalArgumentException("radius cannot be negative!");
        }
        this.radius = radius;
        if (reach < 0) {
            throw new IllegalArgumentException("RTZ teleport reach cannot be negative!");
        }
        this.reach = reach;
        entityMarkerType = EntityMarkerType.defaultType(rewindIds, extractor, injector);
    }

    @Override
    public @NotNull MoveType<ReturnToZeroMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final GEREntity attacker) {
        tickReturnInfo(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final GEREntity attacker, final LivingEntity user) {
        final List<Entity> toReturn = attacker.level().getEntitiesOfClass(Entity.class, attacker.getBoundingBox().inflate(radius),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));

        returnEntityMarkers.clear();
        returnInfo.clear();

        for (final Entity entity : toReturn) {
            if (entityMarkerType.shouldSave(entity.getUUID(), entity)) {
                returnEntityMarkers.add(entityMarkerType.save(entity.getUUID(), entity));
                returnInfo.add(new RewindData(entity.getEyePosition(), entity));
            }
        }
        started = true;

        return Set.of();
    }

    public boolean returnToZero(final GEREntity attacker) {
        if (!started) {
            return false;
        }

        ServerLevel level = (ServerLevel) attacker.level();
        for (final EntityMarker marker : returnEntityMarkers) {
            if (entityMarkerType.shouldLoad(marker, level)  && (JUtils.nullSafeDistanceSqr(level.getEntity(marker.id()), attacker.getUser()) <= reach * reach)) {
                entityMarkerType.load(marker, level);
            }
        }

        // clean up
        started = false;
        returnEntityMarkers.clear();
        returnInfo.clear();

        attacker.playSound(JSoundRegistry.GER_RTZ.get(), 1, 1);
        return true;
    }

    public void tickReturnInfo(final GEREntity attacker) {
        if (!(attacker.getUser() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (final RewindData data : returnInfo) {
            final Entity entity = data.entity();
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            final Vec3 position = data.originalPos();
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeShort(7);

            buf.writeInt(entity.getId());

            buf.writeDouble(position.x());
            buf.writeDouble(position.y());
            buf.writeDouble(position.z());

            ServerChannelFeedbackPacket.send(serverPlayer, buf);
        }
    }

    @Override
    protected @NonNull ReturnToZeroMove getThis() {
        return this;
    }

    @Override
    public @NonNull ReturnToZeroMove copy() {
        return copyExtras(new ReturnToZeroMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getRadius(), getReach(),
                entityMarkerType.getIds(), entityMarkerType.getDataHandler().extractor(), entityMarkerType.getDataHandler().injector()));
    }

    public static class Type extends AbstractMove.Type<ReturnToZeroMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<ReturnToZeroMove>, ReturnToZeroMove> buildCodec(RecordCodecBuilder.Instance<ReturnToZeroMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").forGetter(ReturnToZeroMove::getRadius), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("reach").forGetter(ReturnToZeroMove::getReach), ResourceLocation.CODEC.listOf().xmap(list -> list.stream().collect(Collectors.toSet()), set -> set.stream().toList()).fieldOf("rewindIds").forGetter(move -> move.getEntityMarkerType().getIds()), JRegistries.EXTRACTOR_CODEC.fieldOf("extractor").forGetter(move -> move.getEntityMarkerType().getDataHandler().extractor()), JRegistries.INJECTOR_CODEC.fieldOf("injector").forGetter(move -> move.getEntityMarkerType().getDataHandler().injector())).apply(instance, applyExtras(ReturnToZeroMove::new));
        }
    }
}
