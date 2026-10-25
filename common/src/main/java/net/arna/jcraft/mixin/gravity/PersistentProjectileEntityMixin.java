package net.arna.jcraft.mixin.gravity;

import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin extends Entity {

    public PersistentProjectileEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyVariable(method = "tick", at = @At(value = "STORE"), ordinal = 0)
    public Vec3 gravity$tickProjectile(Vec3 original) {
        final Direction gravity = GravityChangerAPI.getGravityDirection(this);
        if (gravity == Direction.DOWN) return original;
        original = RotationUtil.vecWorldToPlayer(new Vec3(original.x, original.y + 0.05, original.z), gravity);
        original = new Vec3(original.x, original.y - 0.05, original.z);
        return RotationUtil.vecPlayerToWorld(original, gravity);
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V"
            ),
            index = 1
    )
    private static double modifyargs_init_init_0(final double x, final @Local LivingEntity owner) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(owner);
        if (gravityDirection == Direction.DOWN) {
            return x;
        }
        Vec3 pos = owner.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.10000000149011612D, 0.0D, gravityDirection));
        return pos.x;
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V"
            ),
            index = 2
    )
    private static double modifyargs_init_init_1(final double y, final @Local LivingEntity owner) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(owner);
        if (gravityDirection == Direction.DOWN) {
            return y;
        }
        Vec3 pos = owner.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.10000000149011612D, 0.0D, gravityDirection));
        return pos.y;
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;)V"
            ),
            index = 3
    )
    private static double modifyargs_init_init_2(final double z, final @Local LivingEntity owner) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(owner);
        if (gravityDirection == Direction.DOWN) {
            return z;
        }
        Vec3 pos = owner.getEyePosition().subtract(RotationUtil.vecPlayerToWorld(0.0D, 0.10000000149011612D, 0.0D, gravityDirection));
        return pos.z;
    }

}
