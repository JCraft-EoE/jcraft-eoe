package net.arna.jcraft.common.attack.moves.starplatinum;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StarPlatinumEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.attack.moves.AbstractSimpleAttack.createBox;

@Getter
public final class InhaleAttack extends AbstractMove<InhaleAttack, StarPlatinumEntity> {
    private final RandomSource random = RandomSource.create();
    private final int inhaleDuration;

    public InhaleAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final int inhaleDuration) {
        super(cooldown, windup, duration, moveDistance);
        this.inhaleDuration = inhaleDuration;
    }

    @Override
    public @NonNull MoveType<InhaleAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final StarPlatinumEntity attacker) {
        tickInhale(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final StarPlatinumEntity attacker, final LivingEntity user) {
        attacker.setInhaleTime(inhaleDuration);
        return Set.of();
    }

    public void tickInhale(final StarPlatinumEntity attacker) {
        int inhaleTime = attacker.getInhaleTime();
        if (inhaleTime <= 0 || !attacker.hasUser()) {
            return;
        }

        final Vec3 rotVec = attacker.getUserOrThrow().getLookAngle();
        final Vec3 eyePos = attacker.isFree() ?
                new Vec3(attacker.getFreePos()).add(RotationUtil.vecPlayerToWorld(new Vec3(0, attacker.getBbHeight(), 0), GravityChangerAPI.getGravityDirection(attacker)))
                : attacker.getEyePosition();
        final Vec3 fPos = eyePos.add(rotVec.scale(1.75));
        final Vec3 ffPos = eyePos.add(rotVec.scale(3.25));

        // In some rare cases, this appears to be possible, which will crash the game later in the method.
        if (Double.isNaN(fPos.x) || Double.isNaN(fPos.y) || Double.isNaN(fPos.z))
            return;

        if (attacker.level().isClientSide) {
            if (inhaleTime % 20 == 0) attacker.playSound(JSoundRegistry.INHALE_LOOP.get());

            // Display particles for the two hitboxes
            Vec3 addVel = rotVec.add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
            Vec3 particlePos = fPos.add(addVel);

            attacker.level().addParticle(ParticleTypes.POOF,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);

            addVel = rotVec.add(random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75, random.nextDouble() * 1.5 - 0.75);
            particlePos = ffPos.add(addVel);

            attacker.level().addParticle(ParticleTypes.POOF,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    -addVel.x / 10.0, -addVel.y / 10.0, -addVel.z / 10.0);
        } else {
            attacker.setInhaleTime(--inhaleTime);

            if (inhaleTime > 0) {
                attacker.setRotationOffset(90);
            } else {
                attacker.setRotationOffset(225);
            }
            if (attacker.tickCount % 2 != 0) {
                return;
            }

            final AABB fBox = createBox(fPos, 2);
            final AABB ffBox = createBox(ffPos, 2);

            JUtils.displayHitbox(attacker.level(), fBox);
            JUtils.displayHitbox(attacker.level(), ffBox);
            final Set<Entity> hits = AbstractSimpleAttack.findHits(attacker, Set.of(fBox, ffBox), null, Entity.class);

            for (Entity entity : hits) {
                double distance = entity.position().distanceToSqr(eyePos);
                if (distance > 2) { // Fall-off
                    distance -= distance * 0.1;
                }
                if (!entity.onGround()) distance /= 2.0;
                if (distance < 0) distance = 0;

                JUtils.setVelocity(entity,
                        entity.getDeltaMovement()
                        .subtract(rotVec.x, rotVec.y, rotVec.z)
                        .scale(0.1 * distance)
                );
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

    public static class Type extends AbstractMove.Type<InhaleAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<InhaleAttack>, InhaleAttack> buildCodec(RecordCodecBuilder.Instance<InhaleAttack> instance) {
            return baseDefault(instance).and(Codec.INT.fieldOf("inhale_duration").forGetter(InhaleAttack::getInhaleDuration))
                    .apply(instance, applyExtras(InhaleAttack::new));
        }
    }
}
