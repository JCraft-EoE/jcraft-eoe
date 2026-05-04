package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.entity.stand.AerosmithEntity.FlyState;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class AerosmithAttackOrderMove extends AbstractMove<AerosmithAttackOrderMove, AerosmithEntity> {
    @Getter
    private float range;

    @Nullable @Getter
    private LivingEntity currentTarget = null;

    private Vec3 lastFlyTarget;
    private FlyState lastFlyState;

    public AerosmithAttackOrderMove(final int cooldown, final float range) {
        super(cooldown, 0, 0, 0);
        withRange(range);
    }

    public AerosmithAttackOrderMove withRange(final float range) {
        this.range = range;
        return getThis();
    }

    public void clearCurrentTarget() {
        currentTarget = null;
    }

    @Override
    public void onInitiate(final AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();

        if (user == null) return;

        final Vec3 pos = user.position();

        final Vec3 rotVec = user.getLookAngle();

        final BreathXrayMove<AerosmithEntity> xrayMove = attacker.getMove(BreathXrayMove.class);

        if (xrayMove == null) return;

        final var targets = xrayMove.getDetected().object2IntEntrySet();

        final Set<LivingEntity> potentialTargets = new HashSet<>();

        for (var targetEntry : targets) {
            final LivingEntity target = targetEntry.getKey();
            final Vec3 lookVec = target.position().subtract(pos).normalize();

            if (JUtils.angleBetween(lookVec, rotVec) < 0.95) continue;

            potentialTargets.add(target);
        }

        if (targets.isEmpty()) {
            currentTarget = null;
        } else {
            LivingEntity closest = null;
            double minDistanceSq = Double.MAX_VALUE;

            for (LivingEntity entity : potentialTargets) {
                double distanceSq = entity.position().distanceToSqr(pos);
                if (distanceSq < minDistanceSq) {
                    minDistanceSq = distanceSq;
                    closest = entity;
                }
            }

            if (attacker.isRemote()) {
                lastFlyTarget = attacker.getFlyTarget();
                lastFlyState = attacker.getFlyState();
            } else {
                lastFlyTarget = pos;
                lastFlyState = FlyState.RETURN;
            }

            currentTarget = JUtils.getUserIfStand(closest);
        }
    }

    @Override
    public void tick(AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();

        final var bombAttack = attacker.getMove(BombDropAttack.class);

        if (currentTarget == null) return;

        if (!currentTarget.isAlive()) {
            currentTarget = null;

            attacker.setFlyTarget(lastFlyTarget);
            attacker.setFlyState(lastFlyState);
            return;
        }

        if (!attacker.isRemote()) attacker.setRemote(true);

        final var cds = JComponentPlatformUtils.getCooldowns(user);

        if (cds.getCooldown(CooldownType.HEAVY) <= 0.0 && currentTarget.onGround()) {
            final var targetPos = currentTarget.position();

            attacker.setFlyState(FlyState.FLYBY);

            final var bombTarget = targetPos.add(JUtils.getLocalUp(user).scale(6.7));
            attacker.setFlyTarget(bombTarget);

            bombAttack.setDropLocation(bombTarget);

            cds.setCooldown(CooldownType.HEAVY, bombAttack.getCooldown());
        } else {
            if (bombAttack.getDropLocation() == null) {
                if (attacker.getOverheat() < AerosmithEntity.OVERHEAT_MAX / 2f) {
                    attacker.setFlyState(FlyState.FLYBY);

                    final var targetPos = currentTarget.position().add(JUtils.getEyePos(currentTarget));

                    attacker.setFlyTarget(targetPos);

                    final Vec3 lookVec = targetPos.subtract(attacker.position()).normalize();

                    if (JUtils.angleBetween(lookVec, attacker.getLookAngle()) > 0.93)
                        if (attacker.tickCount % 2 == 0)
                            attacker.getMove(MuzzleHitscanAttack.class).perform(attacker, user);
                } else {
                    final var targetPos = currentTarget.position().add(JUtils.getLocalUp(user).scale(16));

                    attacker.setFlyState(FlyState.PATROL);

                    attacker.setFlyTarget(targetPos);
                }
            }
        }

        final var flyTarget = attacker.getFlyTarget();
        JCraft.createParticle((ServerLevel) attacker.level(), flyTarget.x, flyTarget.y, flyTarget.z, JParticleType.BACK_STAB);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public @NonNull MoveType<AerosmithAttackOrderMove> getMoveType() {
        return AerosmithAttackOrderMove.Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull AerosmithAttackOrderMove getThis() {
        return this;
    }

    @Override
    public @NonNull AerosmithAttackOrderMove copy() {
        return copyExtras(new AerosmithAttackOrderMove(getCooldown(), getRange()));
    }

    public static class Type extends AbstractMove.Type<AerosmithAttackOrderMove> {
        public static final AerosmithAttackOrderMove.Type INSTANCE = new AerosmithAttackOrderMove.Type();

        protected RecordCodecBuilder<AerosmithAttackOrderMove, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(AerosmithAttackOrderMove::getRange);
        }

        protected Products.P3<RecordCodecBuilder.Mu<AerosmithAttackOrderMove>, BaseMoveExtras, Integer, Float>
        bombDefault(RecordCodecBuilder.Instance<AerosmithAttackOrderMove> instance) {
            return instance.group(extras(), cooldown(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<AerosmithAttackOrderMove>, AerosmithAttackOrderMove> buildCodec(
                final RecordCodecBuilder.Instance<AerosmithAttackOrderMove> instance) {
            return bombDefault(instance).apply(instance, applyExtras(AerosmithAttackOrderMove::new));
        }
    }
}