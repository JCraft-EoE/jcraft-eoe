package net.arna.jcraft.mixin_logic;

import lombok.NonNull;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.attack.moves.hamon.SendoAttack;
import net.arna.jcraft.common.effects.AbstractFluidWalkingEffect;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class LivingEntityMixinLogic {

    public static double redirect_travel_getY_0(LivingEntity livingEntity) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(livingEntity);
        if (gravityDirection == Direction.DOWN) {
            return livingEntity.getY();
        }

        return RotationUtil.vecWorldToPlayer(livingEntity.position(), gravityDirection).y;
    }

    public static Vec3 modify_travel_Vec3d_2(Entity entity, Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    public static BlockPos modify_playBlockFallSound_getBlockState_0(Entity entity, BlockPos blockPos, Vec3 thisVec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return blockPos;
        }

        return BlockPos.containing(thisVec3d.add(RotationUtil.vecPlayerToWorld(0, -0.20000000298023224D, 0, gravityDirection)));
    }

    public static Vec3 redirect_canSee_new_0(Entity entity, double x, double y, double z, Vec3 eyePos) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return new Vec3(x, y, z);
        }

        return eyePos;
    }

    public static boolean canWalkOnLiquid(final @NonNull Level level, final @NonNull LivingEntity living) {
        final BlockPos below = living.blockPosition().below();
        final BlockPos[] toCheck = new BlockPos[] {
                below//, below.east(), below.east().south(), below.south(), below.west().south(), below.west(), below.west().north(), below.north(), below.east().north()
        };
        if (!level.getFluidState(living.blockPosition().above()).isEmpty()) {
            return false;
        }
        for (BlockPos pos : toCheck) {
            final FluidState state = level.getFluidState(living.blockPosition().below());
            AbstractFluidWalkingEffect[] walkingEffects = new AbstractFluidWalkingEffect[]{
                    JStatusRegistry.WATER_WALKING.get()
            };
            for (AbstractFluidWalkingEffect walkingEffect : walkingEffects) {
                if (living.hasEffect(walkingEffect) && !state.isEmpty() && walkingEffect.supports(state.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void hamonAfterEffect(final LivingEntity user, final Entity target) {
        if (JUtils.getSpec(user) instanceof HamonSpec hamon
                && hamon.willUseHamonNext()
                && hamon.getCharge() >= SendoAttack.CHARGE_COST
                && target instanceof LivingEntity livingTarget
        ) {
            final Level level = user.level();
            if (!level.isClientSide()) {
                final Vec3 position = target.position();
                hamon.drainCharge(SendoAttack.CHARGE_COST);
                hamon.setUseHamonNext(false);
                hamon.processTarget(livingTarget);
                Attacks.damageLogic(level, livingTarget, new AttackData(
                        Vec3.ZERO, 10, StunType.BURSTABLE.ordinal(), false,
                        3f, true, 3, level.damageSources().indirectMagic(user, null),
                        user, CommonHitPropertyComponent.HitAnimation.CRUSH, null,
                        false, false
                ));
                var packet = new ClientboundLevelParticlesPacket(JParticleTypeRegistry.HAMON_SPARK.get(),
                        false,
                        position.x(), position.y(), position.z(),
                        0.1f, 0.1f, 0.1f,
                        0.2f, 1);
                for (ServerPlayer tracker : JUtils.tracking(target)) {
                    tracker.connection.send(packet);
                }
            }
        }
    }
}
