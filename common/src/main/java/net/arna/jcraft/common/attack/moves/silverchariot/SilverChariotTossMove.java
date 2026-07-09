package net.arna.jcraft.common.attack.moves.silverchariot;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/**
 * Silver Chariots throw move. Does not reduce throwing speed while armor off.
 */
public final class SilverChariotTossMove extends AbstractTossMove<SilverChariotTossMove, SilverChariotEntity> {

    public SilverChariotTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public SilverChariotTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public SilverChariotTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<SilverChariotTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SilverChariotEntity attacker, final LivingEntity user) {
        if (attacker.getMode() == SilverChariotEntity.Mode.ARMORLESS) {
            setChargeTime((int)Math.round(getChargeTime() / SilverChariotEntity.ARMORLESS_WINDUP_FACTOR));
        }
        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull SilverChariotTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull SilverChariotTossMove copy() {
        return copyExtras(new SilverChariotTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<SilverChariotTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SilverChariotTossMove>, SilverChariotTossMove> buildCodec(final RecordCodecBuilder.Instance<SilverChariotTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, SilverChariotTossMove::new);
        }
    }
}
