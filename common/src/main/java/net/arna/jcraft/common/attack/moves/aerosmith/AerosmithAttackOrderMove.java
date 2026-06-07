package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.entity.stand.AerosmithEntity.FlyState;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AerosmithAttackOrderMove extends AbstractMove<AerosmithAttackOrderMove, AerosmithEntity> {
    @Getter
    private float range;

    @Nullable @Getter @Setter
    private LivingEntity currentTarget = null;

    private int lockedHitCount = 0;
    private Vec3 lastFlyTarget = Vec3.ZERO;
    private FlyState lastFlyState = FlyState.RETURN;

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
        lockedHitCount = 0;
    }

    public void onHitTarget(AerosmithEntity attacker, LivingEntity target) {
        if (currentTarget == null || target != currentTarget) return;
        if (++lockedHitCount >= 3) {
            lockedHitCount = 0;
            currentTarget = null;
            attacker.triggerForcedReturn();
        }
    }

    @Override
    public boolean conditionsMet(AerosmithEntity attacker) {
        return super.conditionsMet(attacker) && !getTargets(attacker).isEmpty();
    }

    @Override
    public void onInitiate(final AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();
        if (user == null) return;

        final Vec3 pos = user.position();
        final Set<LivingEntity> targets = getTargets(attacker);

        if (targets.isEmpty()) {
            currentTarget = null;
        } else {
            LivingEntity closest = null;
            double minDistanceSq = Double.MAX_VALUE;

            for (LivingEntity entity : targets) {
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

            if (currentTarget != null && user instanceof ServerPlayer serverPlayer) {
                final var targetPosition = currentTarget.position();

                serverPlayer.connection.send(new ClientboundLevelParticlesPacket(JParticleTypeRegistry.SUN_LOCK_ON.get(), true,
                        targetPosition.x, targetPosition.y, targetPosition.z, 0, 0, 0, 0, 1));
            }
        }
    }

    private Set<LivingEntity> getTargets(final AerosmithEntity attacker) {
        LivingEntity user = attacker.getUserOrThrow();

        final Vec3 pos = user.position();
        final Vec3 rotVec = user.getLookAngle();

        final BreathXrayMove<AerosmithEntity> xrayMove = attacker.getBreathXrayMove();
        if (xrayMove == null) return Collections.emptySet();

        final var detected = xrayMove.getDetected().object2IntEntrySet();
        final Set<LivingEntity> targets = new HashSet<>();

        for (var targetEntry : detected) {
            final LivingEntity target = targetEntry.getKey();
            final Vec3 lookVec = target.position().subtract(pos).normalize();

            if (JUtils.angleBetween(lookVec, rotVec) < 0.95) continue;

            targets.add(target);
        }

        return targets;
    }

    @Override
    public void tick(AerosmithEntity attacker) {
        final LivingEntity user = attacker.getUser();

        final var bombAttack = attacker.getBombDropAttack();

        if (currentTarget == null) return;

        if (!currentTarget.isAlive() || currentTarget.isSpectator()) {
            currentTarget = null;

            attacker.setFlyTarget(lastFlyTarget);
            attacker.setFlyState(lastFlyState);
            return;
        }

        if (!attacker.isRemote()) attacker.setRemote(true);

        final var cds = JComponentPlatformUtils.getCooldowns(user);

        if (cds.getCooldown(CooldownType.STAND_HEAVY) <= 0.0 && currentTarget.onGround()) {
            final var targetPos = currentTarget.position();

            attacker.setFlyState(FlyState.FLYBY);

            final var bombTarget = targetPos.add(JUtils.getLocalUp(user).scale(6.7));
            attacker.setFlyTarget(bombTarget);

            bombAttack.setDropLocation(bombTarget);

            cds.setCooldown(CooldownType.STAND_HEAVY, bombAttack.getCooldown());
        } else {
            final var chargeAttack = attacker.getChargeAttack();

            if (bombAttack.getDropLocation() == null) {
                if (attacker.getMoveStun() > 0) {
                    return;
                }

                boolean tryCharge = attacker.getRandom().nextBoolean();

                var targetPos = currentTarget.position();

                final Vec3 lookVec = targetPos.subtract(attacker.position()).normalize();

                if (tryCharge) {
                    if (cds.getCooldown(CooldownType.STAND_BARRAGE) <= 0.0
                            && attacker.distanceToSqr(currentTarget) < 49.0
                            && JUtils.angleBetween(lookVec, attacker.getLookAngle()) > 0.9) {
                        attacker.initMove(MoveClass.BARRAGE);
                        chargeAttack.chargeAt(attacker, currentTarget.position());
                        return;
                    }
                }

                if (attacker.getOverheat() < AerosmithEntity.OVERHEAT_MAX / 2f && !attacker.isInWall()) {
                    attacker.setFlyState(FlyState.FLYBY);

                    targetPos = targetPos.add(JUtils.getEyePos(currentTarget));

                    attacker.setFlyTarget(targetPos);
                } else {
                    targetPos = targetPos.add(JUtils.getLocalUp(user).scale(16));

                    attacker.patrol(targetPos, AerosmithEntity.DEFAULT_PATROL_RADIUS);
                }

                if (JUtils.angleBetween(lookVec, attacker.getLookAngle()) > 0.93)
                    if (attacker.tickCount % 2 == 0) // fire
                        attacker.getShootAttack().perform(attacker, user);
            } else {
                final var bombTarget = currentTarget.position().add(JUtils.getLocalUp(user).scale(6.7));
                bombAttack.setDropLocation(bombTarget);
                attacker.setFlyTarget(bombTarget);
            }
        }

        if (attacker.tickCount % 6 == 0 && user instanceof ServerPlayer serverPlayer) {
            final var targetPos = attacker.getFlyTarget();

            serverPlayer.connection.send(new ClientboundLevelParticlesPacket(JParticleTypeRegistry.SUN_LOCK_ON.get(), true,
                    targetPos.x, targetPos.y, targetPos.z, 0, 0, 0, 0, 1));
        }
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