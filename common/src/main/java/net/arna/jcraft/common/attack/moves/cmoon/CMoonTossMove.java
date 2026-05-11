package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.projectile.ItemTossProjectile;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * C-Moon's throw move. Thrown items have no gravity
 */
public final class CMoonTossMove extends AbstractTossMove<CMoonTossMove, CMoonEntity> {

    public CMoonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public CMoonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public CMoonTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<CMoonTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void onTossed(final CMoonEntity attacker, final LivingEntity user, final Entity thrown) {
        if (thrown == null) return;
        thrown.setNoGravity(true);
    }

    @Override
    protected @NonNull CMoonTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull CMoonTossMove copy() {
        return copyExtras(new CMoonTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<CMoonTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CMoonTossMove>, CMoonTossMove> buildCodec(final RecordCodecBuilder.Instance<CMoonTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, CMoonTossMove::new);
        }
    }
}
