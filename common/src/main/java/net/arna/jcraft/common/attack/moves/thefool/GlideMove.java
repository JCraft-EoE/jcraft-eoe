package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public final class GlideMove extends AbstractMove<GlideMove, TheFoolEntity> {
    public GlideMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.FLIGHT;
    }

    @Override
    public @NonNull MoveType<GlideMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void activeTick(final TheFoolEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        final LivingEntity user = attacker.getUser();
        if (user == null) {
            return;
        }

        user.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 4, 4, true, false));
        double yVel = attacker.getRemoteJumpInput() ? 0.07 : 0;
        if (user.isFallFlying())
            yVel *= 0.001;

        final Vec3 rotVec = user.getLookAngle().scale(0.04);
        user.push(rotVec.x, yVel, rotVec.z);
        user.hurtMarked = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final TheFoolEntity attacker, final LivingEntity user) {
        attacker.setSand(false); // Ends transformation state
        return Set.of();
    }

    @Override
    public boolean preventsMoves() {
        return false;
    }

    @Override
    protected @NonNull GlideMove getThis() {
        return this;
    }

    @Override
    public @NonNull GlideMove copy() {
        return copyExtras(new GlideMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<GlideMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<GlideMove>, GlideMove> buildCodec(RecordCodecBuilder.Instance<GlideMove> instance) {
            return baseDefault(instance, GlideMove::new);
        }
    }
}
