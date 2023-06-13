package net.arna.jcraft.common.entity.damage;

import net.arna.jcraft.common.entity.StandEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;

public class JDamageSources {
    public static DamageSource stand(StandEntity stand, LivingEntity user) {
        return new EntityDamageSource("stand", user).setBypassesArmor();
    }
}
