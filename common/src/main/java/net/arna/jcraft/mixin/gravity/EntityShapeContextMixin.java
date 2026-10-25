package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityCollisionContext.class)
public abstract class EntityShapeContextMixin {

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    private double entityBottom;

    @WrapOperation(
            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getY()D"
            )
    )
    private static double jcraft$init_getY_0(final Entity instance, final Operation<Double> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.boxWorldToPlayer(instance.getBoundingBox(), gravityDirection).minY;
    }

    @WrapMethod(method = "isAbove")
    private boolean jcraft$isAbove(final VoxelShape shape, final BlockPos pos, final boolean canAscend, final Operation<Boolean> original) {
        if (this.entity == null) {
            return original.call(shape, pos, canAscend);
        }
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.entity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(shape, pos, canAscend);
        }
        return this.entityBottom > RotationUtil.boxWorldToPlayer(new AABB(pos), gravityDirection).minY + RotationUtil.boxWorldToPlayer(shape.bounds().inflate(-9.999999747378752e-6), gravityDirection).maxX;
    }

}
