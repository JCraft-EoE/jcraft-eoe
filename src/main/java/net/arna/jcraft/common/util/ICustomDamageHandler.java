package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public interface ICustomDamageHandler {
    boolean reflectsDamage();

    /**
     * @return Whether the damage calculation may continue.
     */
    boolean handleDamage(Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun,
                                  float damage, boolean lift, int blockstun, DamageSource source, Entity attacker,
                                  HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable);
}
