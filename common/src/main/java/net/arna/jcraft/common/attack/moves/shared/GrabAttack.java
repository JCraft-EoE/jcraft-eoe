package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.moves.AbstractGrabAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.util.StandAnimationState;


public final class GrabAttack<A extends IAttacker<A, S>, S extends Enum<S> & StandAnimationState<A>>
        extends AbstractGrabAttack<GrabAttack<A, S>, A, S> {

    public GrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                      final float damage, final int stun, final float hitboxSize, final float knockback,
                      final float offset, final AbstractMove<?, ? super A> hitMove, final StateContainer<S> hitState,
                      final int grabDuration, final double grabOffset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState, grabDuration, grabOffset);
    }

    public GrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                      final float damage, final int stun, final float hitboxSize, final float knockback,
                      final float offset, final AbstractMove<?, ? super A> hitMove, final StateContainer<S> hitState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState);
    }

    @Override
    public @NonNull MoveType<GrabAttack<A, S>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull GrabAttack<A, S> getThis() {
        return this;
    }

    @Override
    public @NonNull GrabAttack<A, S> copy() {
        return copyExtras(new GrabAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMove(), getHitState(), getGrabDuration(), getGrabOffset()));
    }

    public static class Type extends AbstractGrabAttack.Type<GrabAttack<?, ?>> {
        public static final Type INSTANCE = new Type();

        @SuppressWarnings({"rawtypes", "unchecked"}) // checked at runtime
        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<GrabAttack<?, ?>>, GrabAttack<?, ?>> buildCodec(RecordCodecBuilder.Instance<GrabAttack<?, ?>> instance) {
            return grabFullDefault(instance, (cooldown, windup, duration, moveDistance,
                                              damage, stun, hitboxSize, knockback, offset,
                                              hitMove, hitState, grabDuration, grabOffset) ->
                    new GrabAttack(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback,
                            offset, hitMove, hitState, grabDuration, grabOffset));
        }
    }
}
