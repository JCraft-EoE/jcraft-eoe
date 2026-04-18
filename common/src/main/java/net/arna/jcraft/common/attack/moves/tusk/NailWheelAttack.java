package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.TuskAct1Entity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class NailWheelAttack extends AbstractBarrageAttack<NailWheelAttack, TuskAct1Entity> {
    @Getter
    private final float lungeFactor;

    public NailWheelAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset, int interval, float lungeFactor) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.lungeFactor = lungeFactor;
        withoutSlowness(); // Don't slow the user down while dashing
    }

    @Override
    public @NotNull MoveType<NailWheelAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void activeTick(TuskAct1Entity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        if (!attacker.hasUser()) return;

        LivingEntity user = attacker.getUserOrThrow();

        // Scale lunge speed based on nail meter
        float nailRatio = attacker.getNails() / TuskAct1Entity.NAILS_MAX; // 0.0 to 1.0
        float scaledLungeFactor = lungeFactor * nailRatio;

        // Move user forward every tick
        JUtils.addVelocity(user, user.getLookAngle().scale(scaledLungeFactor));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct1Entity attacker, LivingEntity user) {
        // Attack logic is handled by AbstractBarrageAttack
        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull NailWheelAttack getThis() {
        return this;
    }

    @Override
    public @NonNull NailWheelAttack copy() {
        return copyExtras(new NailWheelAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval(), lungeFactor));
    }

    public static class Type extends AbstractBarrageAttack.Type<NailWheelAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<NailWheelAttack>, NailWheelAttack>
        buildCodec(RecordCodecBuilder.Instance<NailWheelAttack> instance) {
            return instance.group(
                    extras(),
                    attackExtras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    damage(),
                    stun(),
                    hitboxSize(),
                    knockback(),
                    offset(),
                    interval(),
                    inflictsSlowness(),
                    Codec.FLOAT.fieldOf("lunge_factor").forGetter(NailWheelAttack::getLungeFactor)
            ).apply(instance, applyAttackExtras((cooldown, windup, duration, moveDistance, damage, stun,
                                                 hitboxSize, knockback, offset, interval, inflictsSlowness, lungeFactor) -> {
                NailWheelAttack attack = new NailWheelAttack(cooldown, windup, duration, moveDistance, damage, stun,
                        hitboxSize, knockback, offset, interval, lungeFactor);
                if (!inflictsSlowness) attack.withoutSlowness();
                return attack;
            }));
        }
    }
}