package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private static double onPlayerMove_playerMovementY;

    @Shadow
    public ServerPlayer player;

    @Shadow
    private static double clampHorizontal(double d) {
        return 0;
    }

    @Shadow
    private static double clampVertical(double d) {
        return 0;
    }

    @Shadow
    private double lastGoodX;

    @Shadow
    private double lastGoodY;

    @Shadow
    private double lastGoodZ;

    @ModifyExpressionValue(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getY()D",
                    ordinal = 3
            ),
            require = 0
    )
    private double modify_onPlayerMove_getY_3(double originalY, @Local(argsOnly = true, index = 1) ServerboundMovePlayerPacket packet) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);

        if (gravityDirection == Direction.DOWN) {
            return originalY;
        }

        return RotationUtil.vecWorldToPlayer(player.position(), gravityDirection).y;
    }

    @WrapOperation(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;doCheckFallDamage(DDDZ)V"
            )
    )
    private void jGravityAPI$doCheckFallDamage(ServerPlayer instance, double movementX, double movementY, double movementZ, boolean onGround, Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);

        if (gravityDirection == Direction.DOWN) {
            original.call(instance, movementX, movementY, movementZ, onGround);
            return;
        }

        Vec3 movement = RotationUtil.vecWorldToPlayer( new Vec3(movementX, movementY, movementZ), gravityDirection);
        original.call(instance, movement.x, movement.y, movement.z, onGround);
    }

    /*
    BASICALLY, MODIFYARGS IS BANNED AND WE HAVE TO FIGURE IT OUT :)
        @ModifyArgs(
            method = "onPlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;handleFall(DDDZ)V",
                    ordinal = 0
            )
    )
    private void modify_onPlayerMove_handleFall_0(Args args) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        Vec3d argVec = new Vec3d(args.get(0), args.get(1), args.get(2));
        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
        args.set(0,argVec.x);
        args.set(1,argVec.y);
        args.set(2,argVec.z);

    }
     */

    @ModifyExpressionValue(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;onGround()Z",
                    ordinal = 0
            )
    )
    private boolean modify_onPlayerMove_boolean_0(boolean value, ServerboundMovePlayerPacket packet) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);

        if (gravityDirection == Direction.DOWN) {
            return value;
        }

        onPlayerMove_playerMovementY = RotationUtil.vecWorldToPlayer(
                clampHorizontal(packet.getX(this.player.getX())) - this.lastGoodX,
                clampVertical(packet.getY(this.player.getY())) - this.lastGoodY,
                clampHorizontal(packet.getZ(this.player.getZ())) - this.lastGoodZ,
                gravityDirection
        ).y;
        return onPlayerMove_playerMovementY > 0.0D;
    }

    @ModifyVariable(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getX()D",
                    ordinal = 5
            ),
            ordinal = 10
    )
    private double modify_onPlayerMove_double_12(double value) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }

        return onPlayerMove_playerMovementY;
    }

    @ModifyArg(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 0
            ),
            index = 1
    )
    private Vec3 modify_onPlayerMove_move_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    //@Redirect(
    //        method = "onVehicleMove",
    //        at = @At(
    //                value = "INVOKE",
    //                target = "Lnet/minecraft/entity/Entity;getY()D",
    //                ordinal = 0
    //        )
    //)
    //private double redirect_onVehicleMove_getY_0(Entity instance) {
    //    Direction gravityDirection = ((EntityAccessor) instance).gravitychanger$getAppliedGravityDirection();
    //    if(gravityDirection == Direction.DOWN) {
    //        return instance.getY();
    //    }
//
    //    return RotationUtil.vecWorldToPlayer(instance.getPos(), gravityDirection).y;
    //}
//
    //@Redirect(
    //        method = "onVehicleMove",
    //        at = @At(
    //                value = "INVOKE",
    //                target = "Lnet/minecraft/entity/Entity;getY()D",
    //                ordinal = 2
    //        )
    //)
    //private double redirect_onVehicleMove_getY_2(Entity instance) {
    //    Direction gravityDirection = ((EntityAccessor) instance).gravitychanger$getAppliedGravityDirection();
    //    if(gravityDirection == Direction.DOWN) {
    //        return instance.getY();
    //    }
//
    //    return RotationUtil.vecWorldToPlayer(instance.getPos(), gravityDirection).y;
    //}

    @ModifyArg(
            method = "handleMoveVehicle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
            ),
            index = 1
    )
    private Vec3 modify_onVehicleMove_move_0(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    //@ModifyVariable(
    //        method = "onVehicleMove",
    //        at = @At(
    //                value = "INVOKE",
    //                target = "Lnet/minecraft/entity/Entity;getX()D",
    //                ordinal = 1
    //        ),ordinal = 0
    //)
    //private double modify_onVehicleMove_double_12(double value) {
    //    Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
    //    if(gravityDirection == Direction.DOWN) {
    //        return value;
    //    }
//
    //    return gravitychanger$onPlayerMove_playerMovementY;
    //}


    @Unique private double xx;
    @Unique private double yy;
    @Unique private double zz;

    @ModifyArg(
            method = "noBlocksAround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 0
    )
    private double modify_onVehicleMove_move_0(double x) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        this.xx = x;
        Vec3 argVec = new Vec3(xx, yy, zz);
        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
        return argVec.x;
    }

    @ModifyArg(
            method = "noBlocksAround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 0
    )
    private double modify_onVehicleMove_move_1(double y) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        this.yy = y;
        Vec3 argVec = new Vec3(xx, yy, zz);
        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
        return argVec.y;
    }
    @ModifyArg(
            method = "noBlocksAround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;expandTowards(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            index = 0
    )
    private double modify_onVehicleMove_move_2(double z) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.player);
        this.zz = z;
        Vec3 argVec = new Vec3(xx, yy, zz);
        argVec = RotationUtil.vecWorldToPlayer(argVec, gravityDirection);
        return argVec.z;
    }

}
