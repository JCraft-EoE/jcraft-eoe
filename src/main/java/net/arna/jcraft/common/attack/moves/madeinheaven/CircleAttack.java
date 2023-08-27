package net.arna.jcraft.common.attack.moves.madeinheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.*;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Set;

public class CircleAttack extends AbstractMove<CircleAttack, MadeInHeavenEntity> {
    public static final IntMoveVariable CIRCLING_TIME = new IntMoveVariable();
    public static final FloatMoveVariable ORBIT_PROG = new FloatMoveVariable();
    public static final MoveVariable<LivingEntity> TARGET = new WeakMoveVariable<>(LivingEntity.class);

    public CircleAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
        mobilityType = MobilityType.DASH;
    }

    @Override
    public boolean onInitialize(MadeInHeavenEntity attacker) {
        if (!super.onInitialize(attacker)) return false;

        LivingEntity user = attacker.getUserOrThrow();
        LivingEntity target = AbstractSimpleAttack.findHits(attacker, user.getEyePos().add(attacker.getRotationVector()), 2d, null)
                .stream()
                .filter(e -> e != attacker && e != user && e instanceof StandEntity<?, ?> stand && stand.hasUser())
                .map(e -> (StandEntity<?, ?>) e)
                .findFirst()
                .map(StandEntity::getUser)
                .orElse(null);

        MoveContext ctx = attacker.getMoveContext();
        attacker.setCirclingTarget(target);
        ctx.set(TARGET, target);
        ctx.setFloat(ORBIT_PROG, user.getHeadYaw());

        return true;
    }

    @Override
    public void onCancel(MadeInHeavenEntity attacker) {
        endCircle(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        ctx.setInt(CIRCLING_TIME, 100);
        attacker.setAfterimage(true);
        attacker.updateRemoteInputs(0, 0, false);
        return Set.of(ctx.get(TARGET));
    }

    public void endCircle(MadeInHeavenEntity attacker) {
        MoveContext ctx = attacker.getMoveContext();
        ctx.setInt(CIRCLING_TIME, 0);
        ctx.set(TARGET, null);
        if (attacker.getAccelTime() <= 0) attacker.setAfterimage(false);
    }

    public void tickCircle(MadeInHeavenEntity attacker) {
        MoveContext ctx = attacker.getMoveContext();
        int circlingTime = ctx.getInt(CIRCLING_TIME);
        if (circlingTime <= 0 || !attacker.hasUser()) return;

        LivingEntity user = attacker.getUserOrThrow();

        if (attacker.getWorld().isClient) {
            Entity clientCircleTarget = attacker.getCircleTarget();
            if (clientCircleTarget != null)
                user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, clientCircleTarget.getEyePos());

            if (attacker.getAccelTime() > 1) { // Updating on the client, to make sure all is smooth
                createSpeedParticles(attacker, attacker);

                List<Entity> toCatch = attacker.getWorld().getEntitiesByClass(Entity.class, // Lower range by 32 to reduce lag
                        attacker.getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toCatch) {
                    if (entity instanceof LivingEntity) continue;
                    if (entity.getPos().squaredDistanceTo(new Vec3d(entity.prevX, entity.prevY, entity.prevZ)) > 0)
                        createSpeedParticles(attacker, entity);
                    entity.tick();
                }
            }

            return;
        }

        ctx.setInt(CIRCLING_TIME, --circlingTime);
        LivingEntity target = ctx.get(TARGET);

        if (target == null || !target.isAlive() || target.isRemoved())
            circlingTime = 1;
        else {
            float orbitProg = ctx.getFloat(ORBIT_PROG);
            ctx.setFloat(ORBIT_PROG, orbitProg += 0.15f);

            boolean toExit = attacker.getCurrentMove() != null && attacker.getCurrentMove().getOriginalMove() != this;
            Vec3d rotVec = user.getRotationVector();
            Vec3d exitVel = Vec3d.ZERO;
            double side = attacker.getRemoteSideInput();
            double forw = attacker.getRemoteForwardInput();

            // This isn't normalized and idc - based
            if (side != 0) {
                exitVel = exitVel.add(rotVec.rotateY(1.5707963f).multiply(side));
                toExit = true;
            }
            if (forw != 0) {
                exitVel = exitVel.add(rotVec.multiply(forw));
                toExit = true;
            }

            if (toExit) {
                user.setVelocity(exitVel.add(0, 0.5, 0));
                endCircle(attacker);
            } else {
                Vec3d orbitPos = target.getEyePos().add(Math.sin(orbitProg) * 7, 0, Math.cos(orbitProg) * 7);
                Vec3d towardsVel = orbitPos.subtract(user.getPos()).normalize();
                double stabilization = user.getPos().distanceTo(orbitPos);
                if (stabilization > 0.5) stabilization = 0.5;
                user.setVelocity(user.getVelocity().multiply(stabilization).add(towardsVel));
            }

            if (user instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
            else user.velocityModified = true;
        }

        if (circlingTime == 1 || user.hasStatusEffect(JStatusRegistry.DAZED)) endCircle(attacker);
    }

    private void createSpeedParticles(MadeInHeavenEntity attacker, Entity entity) {
        Random random = attacker.getRandom();
        Box box = entity.getBoundingBox();
        for (int i = 0; i < box.getAverageSideLength(); i++)
            entity.world.addParticle(JParticleTypeRegistry.SPEED_PARTICLE,
                    random.nextDouble() * box.getXLength() + box.minX,
                    random.nextDouble() * box.getYLength() + box.minY,
                    random.nextDouble() * box.getZLength() + box.minZ,
                    0, 0, 0);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CIRCLING_TIME);
        ctx.register(ORBIT_PROG);
        ctx.register(TARGET);
    }

    @Override
    protected @NonNull CircleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CircleAttack copy() {
        return copyExtras(new CircleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
