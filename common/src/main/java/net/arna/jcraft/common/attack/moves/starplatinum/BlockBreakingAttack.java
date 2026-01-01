package net.arna.jcraft.common.attack.moves.starplatinum;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.AbstractStarPlatinumEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.minecraft.world.level.block.Block.getId;

public final class BlockBreakingAttack extends AbstractSimpleAttack<BlockBreakingAttack, AbstractStarPlatinumEntity<?, ?>> {
    public BlockBreakingAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public void performHook(final AbstractStarPlatinumEntity<?, ?> attacker, final Set<LivingEntity> targets,
                            final Set<AABB> boxes, final DamageSource damageSource, final Vec3 forwardPos,
                            final Vec3 rotationVector) {
        Level world = attacker.level();
        LivingEntity user = attacker.getUserOrThrow();
        if (!mayBreak(user, null)) return;

        final BlockPos bPos = attacker.blockPosition().offset((int) rotationVector.x, (int) rotationVector.y, (int) rotationVector.z);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    final BlockPos curPos = bPos.offset(x, y, z);

                    if (!mayBreak(user, curPos, s -> s.getBlock().getExplosionResistance() <= 10 && !s.isAir())) {
                        continue;
                    }

                    final BlockState curState = world.getBlockState(curPos);
                    world.levelEvent(null, 2001, curPos, getId(curState)); // Particles

                    FallingBlockEntity fallingBlock = FallingBlockEntity.fall(world, curPos, curState);
                    fallingBlock.setDeltaMovement(rotationVector.add(x * 0.5, 0.5, z * 0.5));
                    fallingBlock.time = -120;
                    fallingBlock.hurtMarked = true;
                    fallingBlock.hasImpulse = true;
                }
            }
        }
    }

    @Override
    public @NonNull MoveType<BlockBreakingAttack> getMoveType() {
        return Type.INSTANCE;
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

    public static class Type extends AbstractSimpleAttack.Type<BlockBreakingAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BlockBreakingAttack>, BlockBreakingAttack> buildCodec(RecordCodecBuilder.Instance<BlockBreakingAttack> instance) {
            return attackDefault(instance, BlockBreakingAttack::new);
        }
    }
}
