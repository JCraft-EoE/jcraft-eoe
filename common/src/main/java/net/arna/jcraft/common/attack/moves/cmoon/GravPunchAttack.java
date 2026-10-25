package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class GravPunchAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<GravPunchAttack<A>, A> {
    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravPunchAttack.class.getSimpleName();

    public GravPunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                           final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.INVERTED_HIT_SPARK_3;
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        final Direction oppositeGravity = GravityChangerAPI.getGravityDirection(target).getOpposite();
        GravityChangerAPI.addGravity(target, new Gravity(oppositeGravity, 2, 60, GRAVITY_SOURCE));
        target.addEffect(new MobEffectInstance(JStatusRegistry.WEIGHTLESS.get(), 60, 0, true, false));
        // Launches them up relative to their original gravity, to prevent ground clipping
        JUtils.setVelocity(target, oppositeGravity.getStepX() * 0.2, oppositeGravity.getStepY() * 0.2, oppositeGravity.getStepZ() * 0.2);
    }

    @Override
    public void performHook(final A attacker, final Set<LivingEntity> targets, final Set<AABB> boxes,
                            final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        if (targets.isEmpty()) {
            JCraft.stun(attacker.getUserOrThrow(), 15, 0);
            return;
        }

        LivingEntity baseEntity = attacker.getBaseEntity();
        JComponentPlatformUtils
                .getShockwaveHandler(baseEntity.level())
                .addShockwave(forwardPos, new Vec3(GravityChangerAPI.getGravityDirection(baseEntity).step()), 3.0f);
    }

    @Override
    public @NotNull MoveType<GravPunchAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull GravPunchAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull GravPunchAttack<A> copy() {
        return copyExtras(new GravPunchAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<GravPunchAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<GravPunchAttack<?>>, GravPunchAttack<?>> buildCodec(
                final RecordCodecBuilder.Instance<GravPunchAttack<?>> instance) {
            return attackDefault(instance, GravPunchAttack::new);
        }
    }
}
