package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public final class ThermalShockwaveAttack extends AbstractSimpleAttack<ThermalShockwaveAttack, SpeedKingEntity> {
    private static final int HEAT_PER_HIT = 2;

    private Vec3 lockedDirection = null;
    private Vec3 lockedOrigin = null;

    public ThermalShockwaveAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                  final float damage, final int stun, final float hitboxSize,
                                  final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        copyOnUse = true;
        withStaticY();
    }

    @Override
    public @NonNull MoveType<ThermalShockwaveAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(final SpeedKingEntity attacker) {
        lockedDirection = null;
        lockedOrigin = null;
    }

    /** Fire the hitbox every tick once windup has passed. */
    @Override
    public boolean shouldPerform(final SpeedKingEntity attacker, final int moveStun) {
        return hasWindupPassed(attacker, moveStun);
    }

    /**
     * Each tick the hitbox advances further forward, from 0 to getMoveDistance() over the active duration.
     * The user's position is the origin — stand doesn't move.
     */
    @Override
    protected Vec3 getOffsetForwardPos(final SpeedKingEntity attacker, final Vec3 offsetHeightPos,
                                       final Vec3 upVec, final Vec3 rotVec) {
        if (lockedDirection == null) {
            LivingEntity user = attacker.getUserOrThrow();
            lockedDirection = user.getLookAngle().normalize();
            lockedOrigin = user.position().add(0, user.getBbHeight() * 0.5, 0);
        }
        int elapsed = getDuration() - attacker.getMoveStun();
        double dist = Math.max(0, elapsed - getWindup()) * getMoveDistance();
        return lockedOrigin.add(lockedDirection.scale(dist));
    }

    @Override
    protected void processTarget(final SpeedKingEntity attacker, final LivingEntity target,
                                 final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        LivingEntity user = attacker.getUserOrThrow();
        for (int i = 0; i < HEAT_PER_HIT; i++) {
            HeatTrapManager.addHeat(target, user);
        }
        target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 100, 0, false, true));
        if (lockedDirection != null) {
            target.setDeltaMovement(target.getDeltaMovement().add(lockedDirection.scale(0.6)));
            target.hurtMarked = true;
        }
    }

    @Override
    protected @NonNull ThermalShockwaveAttack getThis() { return this; }

    @Override
    public @NonNull ThermalShockwaveAttack copy() {
        return copyExtras(new ThermalShockwaveAttack(getCooldown(), getWindup(), getDuration(),
                getMoveDistance(), getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<ThermalShockwaveAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ThermalShockwaveAttack>, ThermalShockwaveAttack> buildCodec(RecordCodecBuilder.Instance<ThermalShockwaveAttack> instance) {
            return attackDefault(instance, ThermalShockwaveAttack::new);
        }
    }
}
