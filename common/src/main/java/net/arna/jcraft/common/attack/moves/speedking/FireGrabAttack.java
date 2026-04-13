package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.moves.AbstractGrabAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;

/**
 * Catches the enemy and holds them for {@code grabDuration} ticks.
 * Damage is handled by {@link FireGrabHitAttack} which runs during the hold.
 */
public final class FireGrabAttack extends AbstractGrabAttack<FireGrabAttack, SpeedKingEntity, SpeedKingEntity.State> {

    /** Full constructor. */
    public FireGrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                          final float damage, final int stun, final float hitboxSize, final float knockback,
                          final float offset, final AbstractMove<?, ? super SpeedKingEntity> hitMove,
                          final StateContainer<SpeedKingEntity.State> hitState,
                          final int grabDuration, final double grabOffset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset,
                hitMove, hitState, grabDuration, grabOffset);
    }

    /** Legacy constructor used by the codec — delegates with default grabDuration/grabOffset. */
    public FireGrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                          final float damage, final int stun, final float hitboxSize, final float knockback,
                          final float offset, final AbstractMove<?, ? super SpeedKingEntity> hitMove,
                          final StateContainer<SpeedKingEntity.State> hitState) {
        this(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset,
                hitMove, hitState, 40, 1.0);
    }

    @Override
    public @NonNull MoveType<FireGrabAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull FireGrabAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FireGrabAttack copy() {
        return copyExtras(new FireGrabAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(),
                getHitMove(), getHitState(), getGrabDuration(), getGrabOffset()));
    }

    public static class Type extends AbstractGrabAttack.Type<FireGrabAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireGrabAttack>, FireGrabAttack> buildCodec(RecordCodecBuilder.Instance<FireGrabAttack> instance) {
            return this.<SpeedKingEntity, SpeedKingEntity.State>grabDefault(instance, FireGrabAttack::new);
        }
    }
}
