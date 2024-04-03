package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Set;

public class GroundSlamAttack extends AbstractSimpleAttack<GroundSlamAttack, CMoonEntity> {
    public GroundSlamAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        LivingEntity user = attacker.getUserOrThrow();
        GravityChangerAPI.setWorldVelocity(target, GravityChangerAPI.getGravityDirection(user).getUnitVector());
        target.velocityModified = true;
        if (user.isSneaking())
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 30, 0, true, false));
    }

    @Override
    public void performHook(CMoonEntity attacker, Set<LivingEntity> targets, Set<Box> boxes, DamageSource damageSource, Vec3d forwardPos, Vec3d rotationVector, MoveContext ctx) {
        World world = attacker.getWorld();
        Vec3i gravityVector = GravityChangerAPI.getGravityDirection(attacker).getVector();

        if (world.getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
            BlockPos bPos = attacker.getBlockPos();

            // Adjust pancake shape for gravity
            Vec3i min = new Vec3i(-2, -2, -2);
            Vec3i max = new Vec3i(3, 3, 3);
            min = min.subtract(gravityVector);
            max = max.add(gravityVector);

            for (int x = min.getX(); x < max.getX(); x++) {
                for (int y = min.getY(); y < max.getY(); y++) {
                    for (int z = min.getZ(); z < max.getZ(); z++) {
                        BlockPos curPos = bPos.add(x, y, z);
                        BlockState curState = world.getBlockState(curPos);

                        if (curState.getBlock().getBlastResistance() > 10f || curState.isAir()) continue;

                        FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, curPos, curState);
                        fallingBlock.setVelocity(-gravityVector.getX() * 0.5, -gravityVector.getY() * 0.5, -gravityVector.getZ() * 0.5);
                        fallingBlock.timeFalling = -120;
                        fallingBlock.velocityModified = true;
                        fallingBlock.velocityDirty = true;
                    }
                }
            }
        }

        JComponents.getShockwaveHandler(attacker.getWorld()).addShockwave(attacker.getPos().add(rotationVector), new Vec3d(GravityChangerAPI.getGravityDirection(attacker).getUnitVector()), 4.0f);
    }

    @Override
    protected @NonNull GroundSlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GroundSlamAttack copy() {
        return copyExtras(new GroundSlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
