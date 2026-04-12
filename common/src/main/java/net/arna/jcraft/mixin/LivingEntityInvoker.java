package net.arna.jcraft.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("getDamageAfterArmorAbsorb")
    float invokeApplyArmorToDamage(DamageSource source, float amount);

    @Invoker("getDamageAfterMagicAbsorb")
    float invokeModifyAppliedDamage(DamageSource source, float amount);

    @Accessor("lastHurt")
    void setLastDamageTaken(float lastDamageTaken);

    @Accessor("lastDamageSource")
    void setLastDamageSource(DamageSource damageSource);

    @Accessor("lastDamageStamp")
    void setLastDamageTime(long lastDamageTime);

    @Accessor("lastHurtByPlayerTime")
    void setLastHurtByPlayerTime(int lastHurtByPlayerTime);

    @Invoker
    void callPlayHurtSound(DamageSource source);
}
