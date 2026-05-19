package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Set;

import static net.arna.jcraft.api.attack.moves.AbstractChargeAttack.prepDetachmentMove;

public final class ChargeBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractBarrageAttack<ChargeBarrageAttack<A>, A> {
    private final float originalMoveDistance;
    @Getter
    private final boolean quadraticMovement;

    public ChargeBarrageAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                               final float hitboxSize, final float knockback, final float offset, final int interval, final boolean quadraticMovement) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.originalMoveDistance = moveDistance;
        this.quadraticMovement = quadraticMovement;

        withStunType(StunType.BURSTABLE);
        copyOnUse = true;
        charge = true;
        ranged = true;
        inflictsSlowness = false;
    }

    @Override
    public @NonNull MoveType<ChargeBarrageAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);
        withMoveDistance(originalMoveDistance);
    }

    @Override
    public void activeTick(final A attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        final Entity attackerEntity = attacker.getBaseEntity();
        if (attackerEntity instanceof StandEntity<?, ?> stand) {
            tickChargeBarrageAttack(stand, moveStun < getWindupPoint(), getMoveDistance(), getWindupPoint(), moveStun);
        } else {
            JCraft.LOGGER.error("Trying to tick ChargeBarrageAttack on non-stand entity; {}", attackerEntity);
        }
    }

    private Vec3 advanceChargePos(final StandEntity<?, ?> attacker, final float moveDistance, final int windupPoint, final int moveStun) {
        if (quadraticMovement) {
            return attacker.position().add(getRotVec(attacker).scale(
                    (moveDistance * moveStun) / (windupPoint * windupPoint)
            ));
        }
        return attacker.position().add(getRotVec(attacker).scale(moveDistance / windupPoint));
    }

    private void tickChargeBarrageAttack(final StandEntity<?, ?> attacker, final boolean shouldPerform, final float moveDistance, final int windupPoint, final int moveStun) {
        if (shouldPerform) {
            final Vec3 newPos = advanceChargePos(attacker, moveDistance, windupPoint, moveStun);
            attacker.setFreePos(new Vector3f((float) newPos.x, (float) newPos.y, (float) newPos.z));
            attacker.setFree(true);
        } else {
            prepDetachmentMove(attacker, attacker.getUserOrThrow());
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        final Entity attackerEntity = attacker.getBaseEntity();

        if (targets.isEmpty() || attackerEntity == null) {
            return targets;
        }

        // Moves slower if hit targets are closer, thus not phasing through them.
        Vec3 avgPos = Vec3.ZERO;
        float c = 0;
        for (LivingEntity target : targets) {
            avgPos = avgPos.add(target.position());
            c += 1f;
        }
        avgPos = avgPos.scale(1f / c);
        attackerEntity.lookAt(EntityAnchorArgument.Anchor.EYES, avgPos);
        withMoveDistance((float) avgPos.distanceToSqr(attackerEntity.position()) + 0.1f);

        return targets;
    }

    @Override
    protected Vec3 getOffsetForwardPos(final A attacker, final Vec3 offsetHeightPos, final Vec3 upVec, final Vec3 rotVec) {
        return offsetHeightPos.add(rotVec);
    }

    @Override
    protected @NonNull ChargeBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ChargeBarrageAttack<A> copy() {
        return copyExtras(new ChargeBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), quadraticMovement));
    }

    public static class Type extends AbstractBarrageAttack.Type<ChargeBarrageAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ChargeBarrageAttack<?>>, ChargeBarrageAttack<?>> buildCodec(RecordCodecBuilder.Instance<ChargeBarrageAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                            stun(), hitboxSize(), knockback(), offset(), interval(),
                            Codec.BOOL.fieldOf("quadratic_movement").forGetter(ChargeBarrageAttack::isQuadraticMovement))
                    .apply(instance, applyAttackExtras(ChargeBarrageAttack::new));
        }
    }
}
