package net.arna.jcraft.common.attack.moves.theworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.TheWorldEntity;

public final class TWChargeAttack extends AbstractChargeAttack<TWChargeAttack, TheWorldEntity, TheWorldEntity.State> {
    public TWChargeAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                          final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, TheWorldEntity.State.CHARGE_HIT);
    }

    @Override
    public @NonNull MoveType<TWChargeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull TWChargeAttack getThis() {
        return this;
    }

    @Override
    public void activeTick(final TheWorldEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (moveStun == 1) {
            attacker.setCurrentMove(null);
            attacker.setMoveStun(10);
            attacker.setState(TheWorldEntity.State.CHARGE_MISS);
        }
    }

    @Override
    public @NonNull TWChargeAttack copy() {
        return copyExtras(new TWChargeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractChargeAttack.Type<TWChargeAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TWChargeAttack>, TWChargeAttack> buildCodec(RecordCodecBuilder.Instance<TWChargeAttack> instance) {
            return attackDefault(instance, TWChargeAttack::new);
        }
    }
}
