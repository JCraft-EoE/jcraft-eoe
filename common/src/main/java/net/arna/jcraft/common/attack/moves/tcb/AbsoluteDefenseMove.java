package net.arna.jcraft.common.attack.moves.tcb;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.EnergyCounterProjectile;
import net.arna.jcraft.common.entity.stand.TCBEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class AbsoluteDefenseMove extends AbstractMove<AbsoluteDefenseMove, TCBEntity> {

    public AbsoluteDefenseMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        withHoldable();
    }

    @Override
    public @NotNull MoveType<AbsoluteDefenseMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean conditionsMet(final TCBEntity attacker) {
        return true;
    }

    @Override
    public void onInitiate(final TCBEntity attacker) {
        super.onInitiate(attacker);
        attacker.setAbsoluteDefenseActive(true);
        attacker.resetDefenseTicks();
        if (attacker.hasUser()) {
            JUtils.playAnim(attacker.getUserOrThrow(), "animation.tcb.block");
        }
    }

    @Override
    public void onCancel(final TCBEntity attacker) {
        attacker.setAbsoluteDefenseActive(false);
        attacker.resetDefenseTicks();
        if (attacker.hasUser()) {
            JUtils.playAnim(attacker.getUserOrThrow(), "animation.tcb.reset");
        }
    }

    @Override
    public void activeTick(final TCBEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        LivingEntity user = attacker.getUser();
        if (user == null) return;
        user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 99, true, false));
        user.addEffect(new MobEffectInstance(MobEffects.JUMP, 5, 128, true, false));
        attacker.incrementDefenseTicks();
        if (attacker.getDefenseTicks() == 10) {
            JUtils.playAnim(user, "animation.tcb.block_idle");
        }
    }

    @Override
    public void onUserMoveInput(final TCBEntity attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        super.onUserMoveInput(attacker, type, pressed, moveInitiated);
        if (type.getMoveClass() == getMoveClass() && !pressed) {
            attacker.cancelMove();
        }
    }

    public void handleUserHurt(TCBEntity attacker, DamageSource source, float amount) {
        if (attacker.level().isClientSide()) return;
        Entity attackerEntity = source.getEntity();
        if (attackerEntity == null) return;
        LivingEntity user = attacker.getUserOrThrow();

        double angle = attacker.getRandom().nextDouble() * Math.PI * 2;
        double radius = attacker.getRandom().nextDouble() * 2.0;
        double spawnX = user.getX() + Math.cos(angle) * radius;
        double spawnZ = user.getZ() + Math.sin(angle) * radius;
        double spawnY = user.getY();

        Vec3 spawnPos = new Vec3(spawnX, spawnY, spawnZ);
        Vec3 direction = attackerEntity.position()
                .add(0, attackerEntity.getBbHeight() * 0.5, 0)
                .subtract(spawnPos).normalize();

        EnergyCounterProjectile projectile = new EnergyCounterProjectile(
                attacker.level(), user, amount * 0.75f, direction, attackerEntity);
        projectile.setPos(spawnX, spawnY, spawnZ);
        attacker.level().addFreshEntity(projectile);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final TCBEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    protected @NonNull AbsoluteDefenseMove getThis() {
        return this;
    }

    @Override
    public @NonNull AbsoluteDefenseMove copy() {
        return copyExtras(new AbsoluteDefenseMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<AbsoluteDefenseMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<AbsoluteDefenseMove>, AbsoluteDefenseMove> buildCodec(
                RecordCodecBuilder.Instance<AbsoluteDefenseMove> instance) {
            return baseDefault(instance, AbsoluteDefenseMove::new);
        }
    }
}
