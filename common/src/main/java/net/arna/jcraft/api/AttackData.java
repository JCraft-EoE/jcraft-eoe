package net.arna.jcraft.api;

import lombok.With;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation;
import static net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation.MID;

@With
public record AttackData(Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockStun, DamageSource source, Entity attacker, HitAnimation hitAnimation, MoveUsage moveUsage, boolean canBackstab, boolean unblockable, boolean cancelMoves) {

    public AttackData() {
        this(Vec3.ZERO, 0, 1, false, 0.0f, false, 0, null, null, MID, null, false, false);
    }

    public AttackData(final Vec3 kbVec, final int stunTicks, final int stunLevel, final boolean overrideStun,
                      final float damage, final boolean lift, final int blockStun, final DamageSource source,
                      final Entity attacker, final HitAnimation hitAnimation, final MoveUsage moveUsage,
                      final boolean canBackstab, final boolean unblockable) {
        this(kbVec, stunTicks, stunLevel, overrideStun,
                damage, lift, blockStun, source,
                attacker, hitAnimation, moveUsage,
                canBackstab, unblockable, true);
    }
}
