package net.arna.jcraft.common.attack.moves.theworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class TWDonutAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<TWDonutAttack<A>, A> {
    public TWDonutAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                         final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    public @NonNull MoveType<TWDonutAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        // If missed, stun the user for 1.5 seconds
        if (targets.isEmpty()) {
            JCraft.stun(user, 30, 0);
        }
            /* If hit, impale and set position to middle of arm
        else for (LivingEntity entity : entities) {
            Vec3d pos = this.getPos().add(this.getRotationVector().multiply(1.5));
            entity.teleport(pos.x, entity.getY(), pos.z);
        }*/

        return targets;
    }

    @Override
    public void activeTick(A attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        if (attacker.hasUser() && !attacker.isRemote()) {
            attacker.getUserOrThrow().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2, true, false));
        }
    }

    @Override
    protected @NonNull TWDonutAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TWDonutAttack<A> copy() {
        return copyExtras(new TWDonutAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<TWDonutAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TWDonutAttack<?>>, TWDonutAttack<?>> buildCodec(RecordCodecBuilder.Instance<TWDonutAttack<?>> instance) {
            return attackDefault(instance, TWDonutAttack::new);
        }
    }
}
