package net.arna.jcraft.common.attack.moves.madeinheaven;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Set;

public final class CircleAttack extends AbstractMove<CircleAttack, MadeInHeavenEntity> {
    private int circlingTime = 0;
    private float orbitProg = 0f;
    private WeakReference<LivingEntity> target = new WeakReference<>(null);

    public CircleAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
        mobilityType = MobilityType.DASH;
    }

    @Override
    public @NonNull MoveType<CircleAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(final MadeInHeavenEntity attacker) {
        super.onInitiate(attacker);

        LivingEntity user = attacker.getUserOrThrow();
        LivingEntity target = AbstractSimpleAttack.findHits(attacker, user.getEyePosition().add(attacker.getLookAngle()), 2d, null)
                .stream()
                .map(JUtils::getUserIfStand)
                .findFirst()
                .orElse(null);

        attacker.setCirclingTarget(target);
        this.target = new WeakReference<>(target);
        orbitProg = user.getYHeadRot();
    }

    @Override
    public void onCancel(final MadeInHeavenEntity attacker) {
        endCircle(attacker);
    }

    @Override
    public void tick(MadeInHeavenEntity attacker) {
        super.tick(attacker);

        tickCircle(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final MadeInHeavenEntity attacker, final LivingEntity user) {
        circlingTime = 100;
        attacker.setAfterimage(true);
        attacker.updateRemoteInputs(0, 0, false, false);
        LivingEntity target = this.target.get();
        return target == null ? Set.of() : Set.of(target);
    }

    public void endCircle(final MadeInHeavenEntity attacker) {
        circlingTime = 0;
        target = new WeakReference<>(null);
        attacker.setCirclingTarget(null);
        if (attacker.getAccelTime() <= 0) {
            attacker.setAfterimage(false);
        }
    }

    private void tickCircle(final MadeInHeavenEntity attacker) {
        if (circlingTime <= 0 || !attacker.hasUser()) {
            return;
        }

        LivingEntity user = attacker.getUserOrThrow();

        --circlingTime;
        LivingEntity target = this.target.get();

        if (target == null || !target.isAlive() || target.isRemoved()) {
            circlingTime = 1;
        } else {
            orbitProg += 0.15f;

            boolean toExit = attacker.getCurrentMove() != null && attacker.getCurrentMove() != this;
            Vec3 rotVec = user.getLookAngle();
            Vec3 exitVel = Vec3.ZERO;
            double side = attacker.getRemoteSideInput();
            double forw = attacker.getRemoteForwardInput();

            // This isn't normalized and idc - based
            if (side != 0) {
                exitVel = exitVel.add(rotVec.yRot(1.5707963f).scale(side));
                toExit = true;
            }
            if (forw != 0) {
                exitVel = exitVel.add(rotVec.scale(forw));
                toExit = true;
            }

            final Vec3 newVelocity;
            if (toExit) {
                newVelocity = exitVel.add(0, 0.5, 0);
                endCircle(attacker);
            } else {
                Vec3 orbitPos = target.getEyePosition().add(Math.sin(orbitProg) * 7, 0, Math.cos(orbitProg) * 7);
                Vec3 towardsVel = orbitPos.subtract(user.position()).normalize();
                double stabilization = user.position().distanceTo(orbitPos);
                if (stabilization > 0.5) {
                    stabilization = 0.5;
                }
                newVelocity = user.getDeltaMovement().scale(stabilization).add(towardsVel);
            }

            JUtils.setVelocity(user, newVelocity.x, newVelocity.y, newVelocity.z);
        }

        if (circlingTime == 1 || user.hasEffect(JStatusRegistry.DAZED.get())) {
            endCircle(attacker);
        }
    }

    public static void createSpeedParticles(final MadeInHeavenEntity attacker, final Entity entity) {
        final RandomSource random = attacker.getRandom();
        final AABB box = entity.getBoundingBox();
        for (int i = 0; i < box.getSize(); i++) {
            entity.level().addParticle(JParticleTypeRegistry.SPEED_PARTICLE.get(),
                    random.nextDouble() * box.getXsize() + box.minX,
                    random.nextDouble() * box.getYsize() + box.minY,
                    random.nextDouble() * box.getZsize() + box.minZ,
                    0, 0, 0);
        }
    }

    @Override
    protected @NonNull CircleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CircleAttack copy() {
        return copyExtras(new CircleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<CircleAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CircleAttack>, CircleAttack> buildCodec(RecordCodecBuilder.Instance<CircleAttack> instance) {
            return baseDefault(instance, CircleAttack::new);
        }
    }
}
