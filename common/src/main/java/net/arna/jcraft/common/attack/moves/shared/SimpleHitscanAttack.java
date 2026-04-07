package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHitscanAttack;

public class SimpleHitscanAttack<A extends IAttacker<? extends A, ?>> extends AbstractHitscanAttack<SimpleHitscanAttack<A>, A> {

    public SimpleHitscanAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun, final float knockback, final float range, final float hardness, final float breakChance) {
        super(cooldown, windup, duration, moveDistance, damage, stun, knockback, range, hardness, breakChance);
    }

    @Override
    public @NonNull MoveType<SimpleHitscanAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull SimpleHitscanAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SimpleHitscanAttack<A> copy() {
        return copyExtras(new SimpleHitscanAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getKnockback(), getRange(), getHardness(), getBreakChance()));
    }

    public static class Type extends AbstractHitscanAttack.Type<SimpleHitscanAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SimpleHitscanAttack<?>>, SimpleHitscanAttack<?>> buildCodec(RecordCodecBuilder.Instance<SimpleHitscanAttack<?>> instance) {
            return hitscanDefault(instance, SimpleHitscanAttack::new);
        }
    }
}
