package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.tickable.Timestops;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JCodecUtils;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Getter
public final class TimeSkipMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<TimeSkipMove<A>, A> {
    private final List<Supplier<SoundEvent>> sounds = new ArrayList<>();
    private final double range;
    private boolean particles = false;

    public TimeSkipMove(final int cooldown, final double range, final int windup, final int duration, final float distance) {
        super(cooldown, windup, duration, distance);
        this.range = range;
        mobilityType = MobilityType.TELEPORT;
    }

    public TimeSkipMove(final int cooldown, final double range) {
        this(cooldown, range, 0, 0, 0);
    }

    public TimeSkipMove<A> withSound(SoundEvent sound) {
        sounds.add(() -> sound);
        return this;
    }

    public TimeSkipMove<A> withSound(RegistrySupplier<SoundEvent> sound) {
        sounds.add(sound);
        return this;
    }

    public TimeSkipMove<A> withSounds(Collection<Supplier<SoundEvent>> sounds) {
        this.sounds.addAll(sounds);
        return this;
    }

    public TimeSkipMove<A> withParticles() {
        return withParticles(true);
    }

    public TimeSkipMove<A> withParticles(boolean particles) {
        this.particles = particles;
        return this;
    }

    @Override
    public @NonNull MoveType<TimeSkipMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public boolean conditionsMet(final A attacker) {
        if (Timestops.getTimestop(attacker.getUser()) != null) {
            return false;
        }
        return super.conditionsMet(attacker);
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);

        if (particles) {
            spawnParticles(attacker);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        doTimeSkip(attacker, user, range, getSounds().stream().map(Supplier::get).toList());

        return Set.of();
    }

    private void spawnParticles(final A attacker) {
        final LivingEntity user = attacker.getUserOrThrow();

        final Vec3 pos = user.position();
        final AABB bBox = user.getBoundingBox();

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeShort(2);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(bBox.getXsize());
        buf.writeDouble(bBox.getYsize());
        buf.writeDouble(bBox.getZsize());
        ServerChannelFeedbackPacket.send(JUtils.around((ServerLevel) attacker.getBaseEntity().level(), pos, 128), buf);
    }

    public static void doTimeSkip(final IAttacker<?, ?> attacker, final LivingEntity user, double range, final List<SoundEvent> sounds) {
        final boolean hasVehicle = user.isPassenger();

        if (hasVehicle) {
            range /= 3;
        }

        final Vec3 rotVec = user.getLookAngle();
        //todo: find length of line with direction rotVec, from the center of the stand users bounding box to the edge
        //      then subtract that from the position. this should prevent any TP clipping bullshit
        final Vec3 eyePos = user.getEyePosition();
        HitResult hitResult = attacker.getEntityWorld().clip(
                new ClipContext(
                        eyePos,
                        eyePos.add(rotVec.scale(range)),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, user));
        final Vec3 tpPos = hitResult.getLocation();

        // 3s minimum ult cooldown
        final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) < 60) {
            cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 60);
        }

        if (hasVehicle) {
            final Entity vehicle = user.getVehicle();

            user.dismountVehicle(vehicle);
            vehicle.setPos(tpPos.x, tpPos.y, tpPos.z);
        }
        user.teleportToWithTicket(tpPos.x, tpPos.y, tpPos.z);

        for (SoundEvent sound : sounds) {
            attacker.getEntityWorld().playSound(null, tpPos.x, tpPos.y, tpPos.z, sound, SoundSource.PLAYERS, 1f, 1f);
        }
    }

    @Override
    protected @NonNull TimeSkipMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimeSkipMove<A> copy() {
        return copyExtras(new TimeSkipMove<A>(getCooldown(), range, getWindup(), getDuration(), getMoveDistance())
                .withParticles(particles)
                .withSounds(sounds));
    }

    public static class Type extends AbstractMove.Type<TimeSkipMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TimeSkipMove<?>>, TimeSkipMove<?>> buildCodec(RecordCodecBuilder.Instance<TimeSkipMove<?>> instance) {
            return instance.group(extras(), cooldown(),
                            windup(),
                            duration(),
                            moveDistance(),
                            Codec.DOUBLE.fieldOf("distance").forGetter(TimeSkipMove::getRange),
                            Codec.BOOL.optionalFieldOf("particles", false).forGetter(TimeSkipMove::isParticles),
                            JCodecUtils.SOUND_EVENT_SUPPLIER_CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(TimeSkipMove::getSounds))
                    .apply(instance, applyExtras(
                            (cooldown, windup, duration, moveDistance, distance, particles, sounds) ->
                                    new TimeSkipMove<>(cooldown, distance, windup, duration, moveDistance).withParticles(particles).withSounds(sounds)));
        }
    }
}
