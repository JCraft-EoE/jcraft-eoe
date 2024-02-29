package net.arna.jcraft.common.attack.moves.starplatinum;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StarPlatinumEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Set;

import static net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack.createBox;

@Getter
public class InhaleAttack extends AbstractMove<InhaleAttack, StarPlatinumEntity> {
    private final Random random = Random.create();
    private final int inhaleDuration;

    public InhaleAttack(int cooldown, int windup, int duration, float moveDistance, int inhaleDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.inhaleDuration = inhaleDuration;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(StarPlatinumEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.setInhaleTime(inhaleDuration);
        return Set.of();
    }

    public void tickInhale(StarPlatinumEntity attacker) {
        int inhaleTime = attacker.getInhaleTime();
        if (inhaleTime <= 0 || !attacker.hasUser()) return;

        Vec3d rotVec = attacker.isFree() ? getRotVec(attacker) : attacker.getUserOrThrow().getRotationVector();
        Vec3d eyePos = attacker.isFree() ?
                new Vec3d(attacker.getFreePos()).add(RotationUtil.vecPlayerToWorld(new Vec3d(0, attacker.getHeight(), 0), GravityChangerAPI.getGravityDirection(attacker)))
                : attacker.getEyePos();
        Vec3d fPos = eyePos.add(rotVec.multiply(1.75));
        Vec3d ffPos = eyePos.add(rotVec.multiply(3.25));

        if (attacker.getWorld().isClient) {
            // Display particles for the two hitboxes
            Vec3d addVel = rotVec.add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
            Vec3d particlePos = fPos.add(addVel);

            attacker.getWorld().addParticle(ParticleTypes.POOF,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);

            addVel = rotVec.add(random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75);
            particlePos = ffPos.add(addVel);

            attacker.getWorld().addParticle(ParticleTypes.POOF,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
        } else {
            attacker.setInhaleTime(--inhaleTime);

            if (inhaleTime > 0)
                attacker.setRotationOffset(90);
            else attacker.setRotationOffset(225);
            if (attacker.age % 2 != 0) return;

            Box fBox = createBox(fPos, 2);
            Box ffBox = createBox(ffPos, 2);

            JUtils.displayHitbox(attacker.getWorld(), fBox);
            JUtils.displayHitbox(attacker.getWorld(), ffBox);
            Set<Entity> hits = AbstractSimpleAttack.findHits(attacker, Set.of(fBox, ffBox), null, Entity.class);

            for (Entity entity : hits) {
                double distance = entity.getPos().distanceTo(eyePos);
                if (distance > 3) // Falloff
                    distance -= distance * distance * 0.1;

                entity.setVelocity(entity.getVelocity()
                        .subtract(rotVec.x, 0, rotVec.z)
                        .multiply(0.2 * distance));

                entity.velocityModified = true;

                if (entity instanceof ServerPlayerEntity player)
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            }
        }
    }

    @Override
    protected @NonNull InhaleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull InhaleAttack copy() {
        return copyExtras(new InhaleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getInhaleDuration()));
    }
}
