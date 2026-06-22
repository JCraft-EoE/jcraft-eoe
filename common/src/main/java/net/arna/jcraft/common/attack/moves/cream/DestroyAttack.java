package net.arna.jcraft.common.attack.moves.cream;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public final class DestroyAttack<A extends StandEntity<? extends A, ?>> extends AbstractSimpleAttack<DestroyAttack<A>, A> {
    private final int knockdownDuration;

    public DestroyAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                         final float damage, final int stun, final float hitboxSize, final float knockback,
                         final float offset, final int knockdownDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.knockdownDuration = knockdownDuration;
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        Attacks.trueDamage(8, JDamageSources.stand(attacker), target);
        target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), knockdownDuration, 0, true, false));
    }

    @Override
    public @NotNull MoveType<DestroyAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull DestroyAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull DestroyAttack<A> copy() {
        return copyExtras(new DestroyAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), knockdownDuration));
    }

    public static class Type extends AbstractSimpleAttack.Type<DestroyAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<DestroyAttack<?>>, DestroyAttack<?>> buildCodec(RecordCodecBuilder.Instance<DestroyAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("knockdown_duration").forGetter(DestroyAttack::getKnockdownDuration))
                    .apply(instance, applyAttackExtras(DestroyAttack::new));
        }
    }
}
