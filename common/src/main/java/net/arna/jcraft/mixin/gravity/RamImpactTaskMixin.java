package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RamTarget.class)
public abstract class RamImpactTaskMixin {

    @Shadow
    private Vec3 ramDirection;

    @WrapOperation(
            method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V",
                    ordinal = 0
            )
    )
    private void jcraft$keepRunning_takeKnockback_0(final LivingEntity instance, final double strength, final double x, final double z, final Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            original.call(instance, strength, x, z);
            return;
        }
        Vec3 direction = RotationUtil.vecWorldToPlayer(this.ramDirection, gravityDirection);
        original.call(instance, strength, direction.x, direction.z);
    }

}
