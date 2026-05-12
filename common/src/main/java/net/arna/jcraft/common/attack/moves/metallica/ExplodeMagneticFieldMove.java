package net.arna.jcraft.common.attack.moves.metallica;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.tickable.MagneticFields;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplodeMagneticFieldMove extends AbstractMove<ExplodeMagneticFieldMove, MetallicaEntity> {
    public ExplodeMagneticFieldMove(int cooldown, int windup, int duration) {
        super(cooldown, windup, duration, 0);
    }

    @Override
    public @NonNull MoveType<ExplodeMagneticFieldMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MetallicaEntity attacker, LivingEntity user) {
        final Vec3 eyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult hitResult = JUtils.raycastAll(user, eyePos, eyePos.add(rotVec.scale(64.0)), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE);

        final Vec3 hitPos = hitResult.getLocation();

        final var field = MagneticFields.nearestOfOwnerTo(user, hitPos);

        if (field != null) {
            field.explode();
        }

        return Set.of();
    }

    @Override
    protected @NonNull ExplodeMagneticFieldMove getThis() {
        return this;
    }

    @Override
    public @NonNull ExplodeMagneticFieldMove copy() {
        return copyExtras(new ExplodeMagneticFieldMove(getCooldown(), getWindup(), getDuration()));
    }

    public static class Type extends AbstractMove.Type<ExplodeMagneticFieldMove> {
        public static final ExplodeMagneticFieldMove.Type INSTANCE = new ExplodeMagneticFieldMove.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ExplodeMagneticFieldMove>, ExplodeMagneticFieldMove> buildCodec(RecordCodecBuilder.Instance<ExplodeMagneticFieldMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration()).apply(instance, applyExtras(ExplodeMagneticFieldMove::new));
        }
    }
}
