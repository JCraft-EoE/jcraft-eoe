package net.arna.jcraft.mixin.gravity;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudEntityMixin extends Entity {

    @Shadow
    public abstract boolean isWaiting();

    @Shadow
    public abstract float getRadius();

    @Shadow
    public abstract ParticleOptions getParticle();

    @Shadow
    public abstract int getColor();

    @Shadow
    public abstract void setRadius(float radius);

    @Shadow
    @Nullable
    public abstract LivingEntity getOwner();

    public AreaEffectCloudEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            ),
            index = 1
    )
    private double jcraft$modify_move_multiply_0(double x) {
        return jcraft$mod().x;
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            ),
            index = 2
    )
    private double jcraft$modify_move_multiply_1(double y) {
        return jcraft$mod().y;
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            ),
            index = 3
    )
    private double jcraft$modify_move_multiply_2(double z) {
        return jcraft$mod().z;
    }

    @Unique
    private Vec3 jcraft$mod(){
        boolean bl = this.isWaiting();
        float f = this.getRadius();

        float g;
        if (bl) {
            g = 0.2F;
        } else {
            g = f;
        }

        float h = this.random.nextFloat() * 6.2831855F;
        float k = Mth.sqrt(this.random.nextFloat()) * g;

        double d = this.getX();
        double e = this.getY();
        double l = this.getZ();
        Vec3 modify = RotationUtil.vecWorldToPlayer(d, e, l, GravityChangerAPI.getGravityDirection(this));
        d = modify.x + (double) (Mth.cos(h) * k);
        e = modify.y;
        l = modify.z + (double) (Mth.sin(h) * k);
        modify = RotationUtil.vecPlayerToWorld(d, e, l, GravityChangerAPI.getGravityDirection(this));
        return modify;
    }

}
