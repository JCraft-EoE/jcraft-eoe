package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Magician's Red throw move. Thrown items deal fire damage on hit.
 */
public final class MagiciansRedTossMove extends AbstractTossMove<MagiciansRedTossMove, MagiciansRedEntity> {

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<MagiciansRedTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void onTossed(final MagiciansRedEntity attacker, final LivingEntity user, final Entity thrown) {
        if (thrown != null) {
            thrown.setSecondsOnFire(600); //makes thrown entities appear on fire
        }
    }

    @Override
    protected @NonNull MagiciansRedTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull MagiciansRedTossMove copy() {
        return copyExtras(new MagiciansRedTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<MagiciansRedTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MagiciansRedTossMove>, MagiciansRedTossMove> buildCodec(final RecordCodecBuilder.Instance<MagiciansRedTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, MagiciansRedTossMove::new);
        }
    }
}
