package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.BooleanMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Set;

/**
 * A simple attack that performs at a set interval.
 * The user may crouch to break blocks.
 */
public class MainBarrageAttack<A extends IAttacker<? extends A, ?>> extends AbstractBarrageAttack<MainBarrageAttack<A>, A> {
    public static final BooleanMoveVariable BREAK_BLOCKS = new BooleanMoveVariable();
    private final float mineableHardness;

    public MainBarrageAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset, int interval, float mineableHardness) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, interval);
        this.mineableHardness = mineableHardness;
        withBarrageShockwaves();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        attacker.getMoveContext().setBoolean(BREAK_BLOCKS, attacker.getUserOrThrow().isSneaking());
    }

    @Override
    protected @NonNull MainBarrageAttack<A> getThis() {
        return this;
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(BREAK_BLOCKS, false);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        if (ctx.getBoolean(BREAK_BLOCKS)) {
            ServerWorld serverWorld = (ServerWorld) user.getWorld();
            LivingEntity attackerEntity = attacker.getBaseEntity();
            Vec3i lookDirection = JUtils.getLookDirection(user).getVector();
            Vec3i localUp = GravityChangerAPI.getGravityDirection(user).getOpposite().getVector();

            BlockPos userPos = lookDirection.getY() != 0 ? attackerEntity.getBlockPos() : user.getBlockPos();

            breakIfPossible(serverWorld, userPos.add(lookDirection), user);
            breakIfPossible(serverWorld, userPos.add(lookDirection.add(localUp)), user);
        }

        return targets;
    }

    private void breakIfPossible(ServerWorld world, BlockPos pos, LivingEntity user) {
        Block block = world.getBlockState(pos).getBlock();
        if (block.getHardness() < 0) return;
        if (block.getHardness() <= mineableHardness)
            world.breakBlock(pos, true, user);
    }

    @Override
    public @NonNull MainBarrageAttack<A> copy() {
        return copyExtras(new MainBarrageAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getInterval(), mineableHardness));
    }
}
