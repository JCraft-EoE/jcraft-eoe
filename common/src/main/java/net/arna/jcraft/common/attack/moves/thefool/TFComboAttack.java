package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public final class TFComboAttack<A extends IAttacker<? extends A, ?>> extends AbstractMultiHitAttack<TFComboAttack<A>, A> {
    public TFComboAttack(final int cooldown, final int duration, final float moveDistance, final float damage, int stun, final float hitboxSize,
                         final float knockback, final float offset, final @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull MoveType<TFComboAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (getBlow(attacker) == 2) {
            for (LivingEntity ent : targets) {
                ent.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 20, 0, true, false));
            }
        }

        return targets;
    }

    @Override
    protected @NonNull TFComboAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TFComboAttack<A> copy() {
        return copyExtras(new TFComboAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<TFComboAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TFComboAttack<?>>, TFComboAttack<?>> buildCodec(RecordCodecBuilder.Instance<TFComboAttack<?>> instance) {
            return multiHitDefault(instance, TFComboAttack::new);
        }
    }
}
