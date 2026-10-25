package net.arna.jcraft.api.itfs;

import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface ICustomDamageHandler {
    boolean reflectsDamage();

    /**
     * @return Whether the damage calculation may continue.
     */
    boolean handleDamage(Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun,
                         float damage, boolean lift, int blockstun, DamageSource source, Entity attacker,
                         CommonHitPropertyComponent.HitAnimation hitAnimation, MoveUsage moveUsage,
                         boolean canBackstab, boolean unblockable, boolean cancelAttacks);
}
