package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class CrossfireHurricaneAttack<A extends StandEntity<? extends A, ?>> extends AbstractMove<CrossfireHurricaneAttack<A>, A> {
    private int hurricaneTime = 0;
    private Vec3 hurricanePos = Vec3.ZERO;

    public CrossfireHurricaneAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<CrossfireHurricaneAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void tick(final A attacker) {
        tickHurricane(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        hurricaneTime = 50;
        hurricanePos = attacker.position();
        return Set.of();
    }

    public void tickHurricane(final A stand) {
        // Init variables
        final LivingEntity user = stand.getUserOrThrow();
        final Entity vehicle = user.getVehicle();
        final Level world = stand.level();

        // Run every four ticks because the hurricane's meant to be slow, and it's convenient for CPU usage
        if (stand.tickCount % 4 != 0 || hurricaneTime <= 0) {
            return;
        }
        --hurricaneTime;

        final Predicate<Entity> selector = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != vehicle && e != stand && e != user);

        // Homing
        final List<LivingEntity> nearbyEnts = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(hurricanePos.add(32.0, 32.0, 32.0), hurricanePos.subtract(32.0, 32.0, 32.0)),
                selector);

        if (!nearbyEnts.isEmpty()) {
            Vec3 avgPos = Vec3.ZERO;
            for (LivingEntity livingEntity : nearbyEnts) {
                avgPos = avgPos.add(livingEntity.getEyePosition());
            }
            avgPos = avgPos.scale(1.0 / nearbyEnts.size());

            hurricanePos = hurricanePos.add(avgPos.subtract(hurricanePos).normalize().scale(0.5));
        }

        // Ignite any gasoline splatters at the hurricane's position
        if (!world.isClientSide()) {
            JSplatterManager.get(world)
                    .getHit(hurricanePos, s -> s instanceof GasolineSplatter)
                    .forEach(s -> ((GasolineSplatter) s).lightOnFire());
        }

        // Damage
        final List<LivingEntity> toHurt = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(hurricanePos.add(2.5, 1, 2.5), hurricanePos.subtract(2.5, 1, 2.5)),
                selector);

        for (LivingEntity living : toHurt) {
            LivingEntity target = JUtils.getUserIfStand(living);
            if (hurricaneTime > 1) {
                damageLogic(world, target, new Vec3(Math.sin(stand.tickCount / 10.0) * 3, 0.0, Math.cos(stand.tickCount / 10.0) * 3),
                        10, 1, false, 0.5f, true, 5, JDamageSources.stand(stand), user, CommonHitPropertyComponent.HitAnimation.MID);
                if (hurricaneTime > 15) {
                    hurricaneTime = 15; // Allows for zoning until it hits something
                }
            } else {
                target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 20, 0));
            }
        }

        // Particles
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeShort(10);

        buf.writeDouble(hurricanePos.x);
        buf.writeDouble(hurricanePos.y);
        buf.writeDouble(hurricanePos.z);

        ServerChannelFeedbackPacket.send(JUtils.around((ServerLevel) world, hurricanePos, 128), buf);
    }

    @Override
    protected @NonNull CrossfireHurricaneAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireHurricaneAttack<A> copy() {
        return copyExtras(new CrossfireHurricaneAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<CrossfireHurricaneAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CrossfireHurricaneAttack<?>>, CrossfireHurricaneAttack<?>> buildCodec(RecordCodecBuilder.Instance<CrossfireHurricaneAttack<?>> instance) {
            return baseDefault(instance, CrossfireHurricaneAttack::new);
        }
    }
}