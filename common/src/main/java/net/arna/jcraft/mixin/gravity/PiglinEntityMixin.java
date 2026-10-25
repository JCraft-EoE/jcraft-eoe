package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Piglin.class)
public abstract class PiglinEntityMixin implements CrossbowAttackMob {

    @WrapOperation(
            method = "shootCrossbowProjectile",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;shootCrossbowProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;FF)V",
                    ordinal = 0
            )
    )
    private void jcraft$shoot_shoot_0(final Piglin instance, final LivingEntity living, final LivingEntity target, final Projectile projectile, final float multishotSpray, final float speed, final Operation<Void> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(target);
        if (gravityDirection == Direction.DOWN) {
            original.call(instance, living, target, projectile, multishotSpray, speed);
            return;
        }
        final Vec3 targetPos = target.position().add(RotationUtil.vecPlayerToWorld(0d, target.getBbHeight() * 0.3333333333333333, 0d, gravityDirection));
        final double d = targetPos.x - instance.getX();
        final double e = targetPos.z - instance.getZ();
        final double f = Math.sqrt(Math.sqrt(d * d + e * e));
        final double g = targetPos.y - projectile.getY() + f * 0.20000000298023224D;
        final Vector3f vec3f = this.getProjectileShotVector(instance, new Vec3(d, g, e), multishotSpray);
        projectile.shoot(vec3f.x(), vec3f.y(), vec3f.z(), speed, (float) (14 - instance.level().getDifficulty().getId() * 4));
        instance.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (instance.getRandom().nextFloat() * 0.4F + 0.8F));
    }

}
