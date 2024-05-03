package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Set;

/**
 * A simple attack that performs at a set interval.
 * @param <T>
 * @param <A>
 */
@Getter
public abstract class AbstractBarrageAttack<T extends AbstractBarrageAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<T, A> {
    private final int interval;
    protected boolean inflictsSlowness = true;

    protected AbstractBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage,
                                    int stun, float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        barrage = true;
        this.interval = interval;
        withBlockStun(3);
        withStunType(StunType.WINDED);
        withHitSpark(null);
    }

    public @NonNull T withoutSlowness() {
        this.inflictsSlowness = false;
        return getThis();
    }

    @Override
    protected boolean shouldPerform(A attacker) {
        // If move stun is 22 ticks, windup is 6 and interval is 4, the first hit will occur at tick 6 (when move stun is 22 - 6 = 16),
        // the second at tick 10 (when move stun is 22 - 10 = 12), then at tick 14, etc.
        // For hit 2:
        // move stun = 22, windup = 6, stand move stun = 8 (move stun - windup - 2 * interval)
        // (22 - 6 - 8) % 4 =
        // (16 - 8) % 4 =
        // 8 % 4 = 0

        // This calculation is different from how it used to be done as that was
        // stand move stun % interval == 0
        // Which means that if your move stun is 22, windup is 6 and interval is 6,
        // the first blow will not be landed after 6 ticks (when stand move stun is 22 - 6 = 16),
        // but rather after 10 ticks (when stand move stun is 22 - 10 = 12).
        return attacker.hasUser() && hasWindupPassed(attacker) && (getDuration() - getWindup() - attacker.getMoveStun()) % interval == 0;
    }

    @Override
    public void tick(A attacker) {
        super.tick(attacker);

        // Consider replacing the isRemote() with isFree()?
        if (attacker.hasUser() && inflictsSlowness && !attacker.isRemote()) {
            attacker.getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 2, true, false));
        }
    }

    @Override
    protected Set<LivingEntity> validateTargets(A attacker, Set<LivingEntity> targets) {
        if (!(attacker instanceof StandEntity<?,?> stand)) return targets;

        // Barrage clashing logic.
        for (LivingEntity target : targets) {
            StandEntity<?, ?> targetStand = JUtils.getStand(target);
            Vec3d forwardPos = stand.getRotationVector();
            forwardPos = new Vec3d(stand.getX() + forwardPos.x, stand.getY() + forwardPos.y, stand.getZ() + forwardPos.z);
            if (targetStand == null ||
                    targetStand == attacker ||
                    targetStand.curMove == null ||
                    !targetStand.curMove.isBarrage() ||
                    targetStand.squaredDistanceTo(forwardPos) > 4) {
                continue;
            }
            onClash(attacker.getUserOrThrow());
            onClash(target);

            // Override stun with high priority 0.5s stun, also stops all current sounds for cleaner audio cue
            if (target instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));
            if (attacker.getUserOrThrow() instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new StopSoundS2CPacket(null, SoundCategory.PLAYERS));

            // Cancels both barrages
            stand.cancelMove();
            targetStand.cancelMove();
            Vec3d midPos = attacker.getBaseEntity().getPos().multiply(.5)
                    .add(targetStand.getPos().multiply(.5));
            attacker.getEntityWorld().playSound(null, midPos.x, midPos.y, midPos.z, JSoundRegistry.IMPACT_1, SoundCategory.NEUTRAL, 1, 0.5f);

            return Set.of();
        }

        return targets;
    }

    protected void onClash(LivingEntity entity) {
        entity.removeStatusEffect(JStatusRegistry.DAZED);
        entity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, 10, 3, true, false));
    }

    @Override
    public int getBlow(A attacker) {
        int tick = getDuration() - attacker.getMoveStun();
        return tick <= getWindup() ? 0 : (tick - getWindup()) / getInterval();
    }

    public T withBarrageShockwaves() {
        return this.withAction(((attacker, user, ctx, targets) -> {
            if (targets.isEmpty()) return;
            LivingEntity attackerEntity = attacker.getBaseEntity();
            Random random = attackerEntity.getRandom();
            Vec3d shockwavePos = attackerEntity.getPos().add(
                    random.nextGaussian() / 3.0,
                    random.nextGaussian() / 3.0,
                    random.nextGaussian() / 3.0
            );
            Vec3d rotVec = user.getRotationVector();
            shockwavePos = shockwavePos.add(rotVec);
            shockwavePos = shockwavePos.add(RotationUtil.vecPlayerToWorld(new Vec3d(0, attackerEntity.getHeight() / 1.8 - getOffset(), 0), GravityChangerAPI.getGravityDirection(user)));
            JComponents.getShockwaveHandler(attacker.getEntityWorld())
                    .addShockwave(shockwavePos, user.getRotationVector(), getDamage() / 1.5f);
        }));
    }

    @Override
    protected @NonNull T copyExtras(@NonNull T base) {
        AbstractBarrageAttack<T, A> cast = super.copyExtras(base);
        cast.inflictsSlowness = inflictsSlowness;
        return base;
    }
}
