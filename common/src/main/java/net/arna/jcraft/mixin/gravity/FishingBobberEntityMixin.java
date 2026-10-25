package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FishingHook.class)
public abstract class FishingBobberEntityMixin extends Entity {

    public FishingBobberEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(
            method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"
            )
    )
    private void jcraft$init_(final FishingHook instance, final double x, final double y, final double z, final float yaw, final float pitch, final Operation<Void> original, final Player player, final Level level, final int luck, final int lureSpeed) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            original.call(instance, x, y, z, yaw, pitch);
            return;
        }
        Vec3 pos = player.getEyePosition();
        Vec2 rot = RotationUtil.rotPlayerToWorld(yaw, pitch, gravityDirection);
        original.call(instance, pos.x, pos.y, pos.z, rot.x, rot.y);
    }

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"
            ),
            ordinal = 0
    )
    private Vec3 jcraft$modify_init_Vec3d_1(final Vec3 vec3, final Player player, final Level level, final int luck, final int lureSpeed) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(player);
        if (gravityDirection == Direction.DOWN) {
            return vec3;
        }
        return RotationUtil.vecPlayerToWorld(vec3, gravityDirection);
    }

}
