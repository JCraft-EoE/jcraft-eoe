package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.KQDetonateAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class BTDGrabHitAttack<A extends StandEntity<? extends A, ?>> extends AbstractMultiHitAttack<BTDGrabHitAttack<A>, A> {
    public BTDGrabHitAttack(final int cooldown, final int duration, final float moveDistance, final float damage, final int stun, final float hitboxSize,
                            final float knockback, final float offset, final @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NotNull MoveType<BTDGrabHitAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);
        switch (getBlow(attacker)) {
            case 0 -> {
                for (LivingEntity ent : targets) {
                    ent.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 40, 0, true, false, true));
                }
            }
            case 2 -> KQDetonateAttack.explode(attacker, user, attacker.position().subtract(0, .5, 0));
        }

        return targets;
    }

    @Override
    protected @NonNull BTDGrabHitAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BTDGrabHitAttack<A> copy() {
        return copyExtras(new BTDGrabHitAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<BTDGrabHitAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BTDGrabHitAttack<?>>, BTDGrabHitAttack<?>> buildCodec(RecordCodecBuilder.Instance<BTDGrabHitAttack<?>> instance) {
            return multiHitDefault(instance, BTDGrabHitAttack::new);
        }
    }
}
