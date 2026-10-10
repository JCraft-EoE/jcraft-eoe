package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.effects.FlammableEffect;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.events.EntityTickEvent;
import net.arna.jcraft.common.events.JEntityEvents;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.EntityAddon;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class)
public abstract class EntityMixin implements EntityAddon {

    @Unique
    private boolean jcraft$fromSpawner = false;

    /**
     * Stand positioning mixin function
     *
     * @param passenger stand entity
     */
    @WrapMethod(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V")
    private void jcraft$updatePassengerPosition(final Entity passenger, final Entity.MoveFunction callback, final Operation<Void> original) {
        if (passenger instanceof StandEntity<?, ?> stand) {
            if (stand.isFree() && !stand.isRemote()) {
                Vector3f freePos = stand.getFreePos();
                callback.accept(passenger, freePos.x(), freePos.y(), freePos.z());
                return;
            }

            final double dist = stand.getDistanceOffset();

            final Entity entity = (Entity) (Object) this;
            float y = entity.getYRot() + stand.getRotationOffset();
            y *= Mth.DEG_TO_RAD;

            final Direction gravity = GravityChangerAPI.getGravityDirection(entity);
            final var axis = gravity.getAxis();

            // When the stand should track the user's look pitch (i.e. during attacks), position it along the
            // full look direction at `dist` so it follows where the user is looking, instead of staying at a
            // fixed horizontal distance with only a small vertical nudge.
            float pitch = stand.shouldOffsetHeight() ? entity.getXRot() * Mth.DEG_TO_RAD : 0f;

            if (axis != Direction.Axis.Y) {
                y *= -1.0f;
                pitch += Math.PI;

                if (axis == Direction.Axis.Z) {
                    y += Math.PI;
                }
            }

            final double horizontalDist = dist * Mth.cos(pitch);
            final double heightOffset = -dist * Mth.sin(pitch);

            final Vec3 adjustedOffset = RotationUtil.vecWorldToPlayer(
                    Mth.cos(y) * horizontalDist,
                    passenger.getMyRidingOffset() + heightOffset + stand.getYDistanceOffset(),
                    Mth.sin(y) * horizontalDist,
                    gravity
            );

            callback.accept(passenger, entity.getX() + adjustedOffset.x, entity.getY() + adjustedOffset.y, entity.getZ() + adjustedOffset.z);
            return;
        }
        original.call(passenger, callback);
    }

    @SuppressWarnings("ConstantValue")
    @WrapMethod(method = "setRemainingFireTicks")
    private void jcraft$preventExtinguish(final int remainingFireTicks, final Operation<Void> original) {
        if (remainingFireTicks <= 0 && (Object)this instanceof LivingEntity living && FlammableEffect.isFlammable(living) && living.isOnFire()) {
            return;
        }
        original.call(remainingFireTicks);
    }

    /**
     * Disables sprinting particles during time erase
     */
    @SuppressWarnings("ConstantValue")
    @WrapMethod(method = "canSpawnSprintParticle")
    private boolean jcraft$shouldSpawnSprintingParticles(final Operation<Boolean> original) {
        if ((Object)this instanceof LivingEntity living && JUtils.getStand(living) instanceof KingCrimsonEntity kc && kc.getTETime() > 0) {
            return false;
        }
        return original.call();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void jcraft$preTick(CallbackInfo ci) {
        EntityTickEvent.ENTITY_PRE.invoker().tick((Entity)(Object)this);
    }

    @WrapMethod(method = "isInvulnerable")
    private boolean jcraft$invulnerableIfCreaming(final Operation<Boolean> original) {
        if (CreamEntity.isCreaming((Entity)(Object)this)) {
            return true;
        }
        return original.call();
    }

    @SuppressWarnings("ConstantValue")
    @ModifyExpressionValue(method = "isInvulnerableTo", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;invulnerable:Z", opcode = Opcodes.GETFIELD))
    private boolean jcraft$invulnerableIfCreaming(final boolean original) {
        return original || CreamEntity.isCreaming((Entity)(Object)this);
    }

    @WrapMethod(method = "checkInsideBlocks")
    private void jcraft$ignoreBlockPushingIfCreaming(final Operation<Void> original) {
        if (CreamEntity.isCreaming((Entity) (Object) this)) {
            return;
        }
        original.call();
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    private void jcraft$fireRemovedEvent(Entity.RemovalReason removalReason, CallbackInfo ci) {
        JEntityEvents.REMOVE.invoker().remove((Entity)(Object)this, removalReason);
    }

    @Override
    public boolean jcraft$setFromSpawner() {
        return jcraft$fromSpawner = true;
    }

    @Override
    public boolean jcraft$isFromSpawner() {
        return jcraft$fromSpawner;
    }

}
