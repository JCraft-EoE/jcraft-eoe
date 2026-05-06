package net.arna.jcraft.common.attack.moves.toss.special;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.attack.moves.toss.AbstractTossMove;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * KQ's throw move. Throws the main hand item as a projectile with KQ's main bomb planted on it.
 * The bomb only detonates when the player manually triggers detonation.
 */
public final class KQTossMove extends AbstractTossMove<KQTossMove, AbstractKillerQueenEntity<?, ?>> {

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<KQTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void onTossed(final AbstractKillerQueenEntity<?, ?> attacker, final LivingEntity user, final Entity thrown) {
        if (thrown != null) {
            JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(thrown);
        }
        attacker.playSound(JSoundRegistry.TOSS.get());
    }

    @Override
    protected @NonNull KQTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull KQTossMove copy() {
        return copyExtras(new KQTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<KQTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQTossMove>, KQTossMove> buildCodec(final RecordCodecBuilder.Instance<KQTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance()).apply(instance, KQTossMove::new);
        }
    }
}
