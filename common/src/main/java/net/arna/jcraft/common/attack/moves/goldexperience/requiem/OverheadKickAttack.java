package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class OverheadKickAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<OverheadKickAttack<A>, A> {
    public OverheadKickAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                              final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        JUtils.addVelocity(target, 0, -1, 0);
        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 5, 23, false, false));
    }

    @Override
    public @NotNull MoveType<OverheadKickAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull OverheadKickAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull OverheadKickAttack<A> copy() {
        return copyExtras(new OverheadKickAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<OverheadKickAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<OverheadKickAttack<?>>, OverheadKickAttack<?>> buildCodec(RecordCodecBuilder.Instance<OverheadKickAttack<?>> instance) {
            return attackDefault(instance, OverheadKickAttack::new);
        }
    }
}
