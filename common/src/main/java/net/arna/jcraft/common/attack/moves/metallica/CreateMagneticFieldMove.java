package net.arna.jcraft.common.attack.moves.metallica;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.tickable.MagneticFields;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class CreateMagneticFieldMove extends AbstractMove<CreateMagneticFieldMove, MetallicaEntity> {
    public static final float IRON_COST = 10.0f;

    public CreateMagneticFieldMove(int cooldown, int windup, int duration) {
        super(cooldown, windup, duration, 0);
    }

    @Override
    public @NonNull MoveType<CreateMagneticFieldMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MetallicaEntity attacker, LivingEntity user) {
        final Vec3 eyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult hitResult = JUtils.raycastAll(user, eyePos, eyePos.add(rotVec.scale(12.0)), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        // JCraft.createParticle((ServerLevel) user.level(), hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, JParticleType.STUN_PIERCE);

        final Vec3 hitPos = hitResult.getLocation();

        JComponentPlatformUtils.getCooldowns(user).setCooldown(CooldownType.STAND_SP2, 400);

        MagneticFields.createField(
                (ServerLevel) user.level(),
                user,
                hitPos.subtract(
                        Vec3.atLowerCornerOf(
                                GravityChangerAPI.getGravityDirection(user).getNormal()
                        ).scale(2.0)
                )
        );

        attacker.drainIron(IRON_COST);

        return Set.of();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(MetallicaEntity attacker, LivingEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return (attacker.distanceToSqr(target) > 4.0 && attacker.getRandom().nextFloat() < 0.03f) ? MoveSelectionResult.USE : MoveSelectionResult.PASS;
    }

    @Override
    protected @NonNull CreateMagneticFieldMove getThis() {
        return this;
    }

    @Override
    public @NonNull CreateMagneticFieldMove copy() {
        return copyExtras(new CreateMagneticFieldMove(getCooldown(), getWindup(), getDuration()));
    }

    public static class Type extends AbstractMove.Type<CreateMagneticFieldMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CreateMagneticFieldMove>, CreateMagneticFieldMove> buildCodec(RecordCodecBuilder.Instance<CreateMagneticFieldMove> instance) {
            return instance.group(extras(), cooldown(), windup(), duration()).apply(instance, applyExtras(CreateMagneticFieldMove::new));
        }
    }
}
