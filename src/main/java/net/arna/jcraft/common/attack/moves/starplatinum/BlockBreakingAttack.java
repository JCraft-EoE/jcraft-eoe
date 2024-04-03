package net.arna.jcraft.common.attack.moves.starplatinum;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.AbstractStarPlatinumEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;

import static net.minecraft.block.Block.getRawIdFromState;

public class BlockBreakingAttack extends AbstractSimpleAttack<BlockBreakingAttack, AbstractStarPlatinumEntity<?, ?>> {
    public BlockBreakingAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public void performHook(AbstractStarPlatinumEntity<?, ?> attacker, Set<LivingEntity> targets, Set<Box> boxes, DamageSource damageSource, Vec3d forwardPos, Vec3d rotationVector, MoveContext ctx) {
        World world = attacker.getWorld();
        if (world.getGameRules().getBoolean(JCraft.STAND_GRIEFING)) {
            BlockPos bPos = attacker.getBlockPos().add((int)rotationVector.x * 1, (int)rotationVector.y * 1, (int)rotationVector.z * 1);
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        BlockPos curPos = bPos.add(x, y, z);
                        BlockState curState = world.getBlockState(curPos);

                        if (curState.getBlock().getBlastResistance() > 10f || curState.isAir()) continue;

                        world.syncWorldEvent(null, 2001, curPos, getRawIdFromState(curState)); // Particles

                        FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, curPos, curState);
                        fallingBlock.setVelocity(rotationVector.add(x * 0.5, 0.5, z * 0.5));
                        fallingBlock.timeFalling = -120;
                        fallingBlock.velocityModified = true;
                        fallingBlock.velocityDirty = true;
                    }
                }
            }
        }
    }

    @Override
    protected @NonNull BlockBreakingAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BlockBreakingAttack copy() {
        return copyExtras(new BlockBreakingAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
