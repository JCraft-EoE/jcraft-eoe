package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * KQ's throw move. Throws the main hand item as a projectile with KQ's main bomb planted on it.
 * The bomb only detonates when the player manually triggers detonation.
 */
public final class KQTossMove<A extends IAttacker<? extends A, ?>> extends AbstractTossMove<KQTossMove<A>, A> {

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<KQTossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected void onTossed(final A attacker, final LivingEntity user, final Entity thrown) {
        if (thrown != null) {
            JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(thrown);
        }
    }

    @Override
    protected @NonNull KQTossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull KQTossMove<A> copy() {
        return copyExtras(new KQTossMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<KQTossMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQTossMove<?>>, KQTossMove<?>> buildCodec(final RecordCodecBuilder.Instance<KQTossMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, KQTossMove::new);
        }
    }
}
