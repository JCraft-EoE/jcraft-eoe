package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Tusk Act 3 - Nail Swipes (Barrage)
 * Quick startup, 5 hits. Medium cooldown. Mediocre stun.
 */
public final class NailBarrageAttack extends AbstractBarrageAttack<NailBarrageAttack, TuskAct3Entity> {

    public NailBarrageAttack(int cooldown, int windup, int duration, float moveDistance,
                             float damage, int stun, float hitboxSize, float knockback,
                             float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NotNull MoveType<NailBarrageAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull NailBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull NailBarrageAttack copy() {
        return copyExtras(new NailBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<NailBarrageAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<NailBarrageAttack>, NailBarrageAttack>
        buildCodec(RecordCodecBuilder.Instance<NailBarrageAttack> instance) {
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
                    inflictsSlowness()
            ).apply(instance, applyAttackExtras((cooldown, windup, duration, moveDistance, damage, stun,
                                                 hitboxSize, knockback, offset, interval, inflictsSlowness) -> {
                NailBarrageAttack attack = new NailBarrageAttack(cooldown, windup, duration, moveDistance,
                        damage, stun, hitboxSize, knockback, offset, interval);
                if (!inflictsSlowness) attack.withoutSlowness();
                return attack;
            }));
        }
    }
}
