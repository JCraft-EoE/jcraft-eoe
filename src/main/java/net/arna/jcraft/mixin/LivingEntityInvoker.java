package net.arna.jcraft.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("applyArmorToDamage")
    float invokeApplyArmorToDamage(DamageSource source, float amount);

    @Invoker("modifyAppliedDamage")
    float invokeModifyAppliedDamage(DamageSource source, float amount);

    @Accessor("lastDamageTaken")
    void setLastDamageTaken(float lastDamageTaken);

    @Accessor("lastDamageSource")
    void setLastDamageSource(DamageSource damageSource);

    @Accessor("lastDamageTime")
    void setLastDamageTime(long lastDamageTime);
}