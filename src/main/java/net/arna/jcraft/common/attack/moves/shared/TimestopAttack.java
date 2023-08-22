package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.moves.base.AbstractTimestopAttack;

@Getter
public class TimestopAttack<A extends IAttacker<?, ?>> extends AbstractTimestopAttack<TimestopAttack<A>, A> {
    public TimestopAttack(int cooldown, int windup, int duration, float moveDistance, int timestopticks) {
        super(cooldown, windup, duration, moveDistance, timestopticks);
    }

    @Override
    protected @NonNull TimestopAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimestopAttack<A> copy() {
        return copyExtras(new TimestopAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getTimestopDuration()));
    }
}
