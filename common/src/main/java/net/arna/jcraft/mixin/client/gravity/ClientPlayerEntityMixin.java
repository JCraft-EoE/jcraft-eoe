package net.arna.jcraft.mixin.client.gravity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract boolean suffocatesAt(BlockPos pos);

    @WrapOperation(
            method = "suffocatesAt",
            at = @At(
                    value = "NEW",
                    target = "(DDDDDD)Lnet/minecraft/world/phys/AABB;",
                    ordinal = 0
            )
    )
    private AABB jcraft$wouldCollideAt_new_0(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2, final Operation<AABB> original, final BlockPos pos) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) {
            return original.call(x1, y1, z1, x2, y2, z2);
        }

        AABB playerBox = this.getBoundingBox();
        Vec3 playerMask = RotationUtil.maskPlayerToWorld(0.0D, 1.0D, 0.0D, gravityDirection);
        AABB posBox = new AABB(pos);
        Vec3 posMask = RotationUtil.maskPlayerToWorld(1.0D, 0.0D, 1.0D, gravityDirection);

        return new AABB(
                playerMask.multiply(playerBox.minX, playerBox.minY, playerBox.minZ).add(posMask.multiply(posBox.minX, posBox.minY, posBox.minZ)),
                playerMask.multiply(playerBox.maxX, playerBox.maxY, playerBox.maxZ).add(posMask.multiply(posBox.maxX, posBox.maxY, posBox.maxZ))
        );
    }

    @WrapMethod(method = "moveTowardsClosestSpace")
    private void jcraft$pushOutOfBlocks(final double x, final double z, final Operation<Void> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this);
        if (gravityDirection == Direction.DOWN) {
            original.call(x, z);
            return;
        }

        Vec3 pos = RotationUtil.vecPlayerToWorld(x - this.getX(), 0d, z - this.getZ(), gravityDirection).add(this.position());
        BlockPos blockPos = BlockPos.containing(pos);
        if (this.suffocatesAt(blockPos)) {
            double dx = pos.x - (double) blockPos.getX();
            double dy = pos.y - (double) blockPos.getY();
            double dz = pos.z - (double) blockPos.getZ();
            Direction direction = null;
            double minDistToEdge = Double.MAX_VALUE;

            Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
            for (Direction playerDirection : directions) {
                Direction worldDirection = RotationUtil.dirPlayerToWorld(playerDirection, gravityDirection);

                double g = worldDirection.getAxis().choose(dx, dy, dz);
                double distToEdge = worldDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - g : g;
                if (distToEdge < minDistToEdge && !this.suffocatesAt(blockPos.relative(worldDirection))) {
                    minDistToEdge = distToEdge;
                    direction = playerDirection;
                }
            }

            if (direction != null) {
                Vec3 velocity = this.getDeltaMovement();
                if (direction.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1D * (double) direction.getStepX(), velocity.y, velocity.z);
                } else if (direction.getAxis() == Direction.Axis.Z) {
                    this.setDeltaMovement(velocity.x, velocity.y, 0.1D * (double) direction.getStepZ());
                }
            }
        }
    }

}
