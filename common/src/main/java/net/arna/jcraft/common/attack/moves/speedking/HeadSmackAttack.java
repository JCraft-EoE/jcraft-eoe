package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;


@Getter
public final class HeadSmackAttack extends AbstractSimpleAttack<HeadSmackAttack, SpeedKingEntity> {
    private final int knockdownDuration;
    private final int blindnessDuration;

    public HeadSmackAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback,
                           final float offset, final int knockdownDuration, final int blindnessDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.knockdownDuration = knockdownDuration;
        this.blindnessDuration = blindnessDuration;
    }

    @Override
    public @NonNull MoveType<HeadSmackAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void processTarget(SpeedKingEntity attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        // Apply knockdown effect
        target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), knockdownDuration, 0, true, false));

        // Apply blindness effect
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindnessDuration, 0, true, false));
    }

    @Override
    protected @NonNull HeadSmackAttack getThis() {
        return this;
    }

    @Override
    public @NonNull HeadSmackAttack copy() {
        HeadSmackAttack copy = new HeadSmackAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(),
                knockdownDuration, blindnessDuration);
        return copyExtras(copy);
    }

    public static class Type extends AbstractSimpleAttack.Type<HeadSmackAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeadSmackAttack>, HeadSmackAttack> buildCodec(RecordCodecBuilder.Instance<HeadSmackAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                            stun(), hitboxSize(), knockback(), offset(),
                            Codec.INT.fieldOf("knockdown_duration").forGetter(HeadSmackAttack::getKnockdownDuration),
                            Codec.INT.fieldOf("blindness_duration").forGetter(HeadSmackAttack::getBlindnessDuration))
                    .apply(instance, applyAttackExtras(HeadSmackAttack::new));
        }
    }
}