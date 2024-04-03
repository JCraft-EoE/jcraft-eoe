package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class CrossfireHurricaneAttack extends AbstractMove<CrossfireHurricaneAttack, MagiciansRedEntity> {
    public static final IntMoveVariable HURRICANE_TIME = new IntMoveVariable();
    public static final MoveVariable<Vec3d> HURRICANE_POS = new MoveVariable<>(Vec3d.class);

    public CrossfireHurricaneAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        ctx.setInt(HURRICANE_TIME, 50);
        ctx.set(HURRICANE_POS, attacker.getPos());
        return Set.of();
    }

    public void tickHurricane(MagiciansRedEntity stand) {
        // Init variables
        MoveContext ctx = stand.getMoveContext();
        int hurricaneTime = ctx.getInt(HURRICANE_TIME);
        Vec3d hurricanePos = ctx.get(HURRICANE_POS);
        LivingEntity user = stand.getUserOrThrow();
        Entity vehicle = user.getVehicle();
        World world = stand.getWorld();

        // Run every four ticks because the hurricane's meant to be slow, and it's convenient for CPU usage
        if (stand.age % 4 != 0 || hurricaneTime <= 0) return;
        ctx.setInt(HURRICANE_TIME, --hurricaneTime);

        // Homing
        List<LivingEntity> nearbyEnts = world.getEntitiesByClass(LivingEntity.class,
                new Box(hurricanePos.add(32.0, 32.0, 32.0), hurricanePos.subtract(32.0, 32.0, 32.0)),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != vehicle && e != stand && e != user));

        if (!nearbyEnts.isEmpty()) {
            Vec3d avgPos = Vec3d.ZERO;
            for (LivingEntity livingEntity : nearbyEnts)
                avgPos = avgPos.add(livingEntity.getEyePos());
            avgPos = avgPos.multiply(1.0 / nearbyEnts.size());

            ctx.set(HURRICANE_POS, hurricanePos = hurricanePos.add(avgPos.subtract(hurricanePos).normalize().multiply(0.5)));
        }

        // Damage
        List<LivingEntity> toHurt = world.getEntitiesByClass(LivingEntity.class,
                new Box(hurricanePos.add(2.5, 1, 2.5), hurricanePos.subtract(2.5, 1, 2.5)),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != user && e != vehicle));

        for (LivingEntity living : toHurt) {
            LivingEntity target = JUtils.getUserIfStand(living);
            if (hurricaneTime > 1) {
                StandEntity.damageLogic(world, target, new Vec3d(Math.sin(stand.age / 10.0) * 3, 0.0, Math.cos(stand.age / 10.0) * 3),
                        10, 1, false, 0.5f, true, 5, JDamageSources.stand(stand), user, HitPropertyComponent.HitAnimation.MID);
                if (hurricaneTime > 15)
                    ctx.setInt(HURRICANE_TIME, 15); // Allows for zoning up until it hits something
            } else target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 20, 0));
        }

        // Particles
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(10);

        buf.writeDouble(hurricanePos.x);
        buf.writeDouble(hurricanePos.y);
        buf.writeDouble(hurricanePos.z);

        for (ServerPlayerEntity sendPlayer : ((ServerWorld) world).getPlayers())
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(HURRICANE_TIME);
        ctx.register(HURRICANE_POS);
    }

    @Override
    protected @NonNull CrossfireHurricaneAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireHurricaneAttack copy() {
        return copyExtras(new CrossfireHurricaneAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
