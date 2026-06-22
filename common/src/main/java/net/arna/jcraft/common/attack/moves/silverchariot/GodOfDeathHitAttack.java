package net.arna.jcraft.common.attack.moves.silverchariot;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public final class GodOfDeathHitAttack<A extends IAttacker<? extends A, ?>> extends AbstractMultiHitAttack<GodOfDeathHitAttack<A>, A> {
    public GodOfDeathHitAttack(final int cooldown, final int duration, final float moveDistance, final float damage, final int stun,
                               final float hitboxSize, final float knockback, final float offset, final IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull MoveType<GodOfDeathHitAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (getBlow(attacker) == 1) {
            attacker.setCurrentMove(getFollowup());
        }

        return targets;
    }

    @Override
    protected @NonNull GodOfDeathHitAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull GodOfDeathHitAttack<A> copy() {
        return copyExtras(new GodOfDeathHitAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<GodOfDeathHitAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<GodOfDeathHitAttack<?>>, GodOfDeathHitAttack<?>> buildCodec(RecordCodecBuilder.Instance<GodOfDeathHitAttack<?>> instance) {
            return multiHitDefault(instance, GodOfDeathHitAttack::new);
        }
    }
}
