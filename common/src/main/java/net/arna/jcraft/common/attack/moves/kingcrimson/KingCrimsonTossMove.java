package net.arna.jcraft.common.attack.moves.kingcrimson;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/**
 * King Crimson throw move. Ends Time Erase.
 */
public final class KingCrimsonTossMove extends AbstractTossMove<KingCrimsonTossMove, KingCrimsonEntity> {

    public KingCrimsonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public KingCrimsonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public KingCrimsonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<KingCrimsonTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final KingCrimsonEntity attacker, final LivingEntity user) {
        if (attacker.getTETime() > 0) {
            final TimeEraseMove te = attacker.getTimeEraseMove();
            if (te != null) {
                te.cancelTE(attacker);
            }
        }
        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull KingCrimsonTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull KingCrimsonTossMove copy() {
        return copyExtras(new KingCrimsonTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<KingCrimsonTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KingCrimsonTossMove>, KingCrimsonTossMove> buildCodec(final RecordCodecBuilder.Instance<KingCrimsonTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, KingCrimsonTossMove::new);
        }
    }
}
