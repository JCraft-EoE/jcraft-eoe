package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.KnockdownAttack;
import net.arna.jcraft.api.stand.StandEntity;

public final class LungeAttack<A extends StandEntity<? extends A, ?>> extends AbstractSimpleAttack<LungeAttack<A>, A> {
    private final float originalMoveDistance;
    @Getter
    private final int beginMoveStun, endMoveStun;

    public LungeAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                       final float hitboxSize, final float knockback, final float offset, final int beginMoveStun, final int endMoveStun) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        originalMoveDistance = moveDistance;
        this.beginMoveStun = beginMoveStun;
        this.endMoveStun = endMoveStun;
        if (endMoveStun >= beginMoveStun) throw new IllegalStateException("End movestun must be smaller than starting!");
        this.ranged = true;
    }

    @Override
    public @NonNull MoveType<LungeAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        // Reset move distance
        withMoveDistance(originalMoveDistance);
    }

    @Override
    public void activeTick(final A attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (moveStun > endMoveStun && moveStun <= beginMoveStun) {
            withMoveDistance(getMoveDistance() + 0.15f);
        }
    }

    @Override
    protected @NonNull LungeAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LungeAttack<A> copy() {
        return copyExtras(new LungeAttack<>(getCooldown(), getWindup(), getDuration(), originalMoveDistance, getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), beginMoveStun, endMoveStun));
    }

    public static class Type extends AbstractSimpleAttack.Type<LungeAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<LungeAttack<?>>, LungeAttack<?>> buildCodec(RecordCodecBuilder.Instance<LungeAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(),
                    Codec.INT.fieldOf("begin_move_stun").forGetter(LungeAttack::getBeginMoveStun),
                    Codec.INT.fieldOf("end_move_stun").forGetter(LungeAttack::getEndMoveStun))
                    .apply(instance, applyAttackExtras(LungeAttack::new));
        }
    }
}
