package net.arna.jcraft.common.attack.moves.goldexperience;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public class BerryBushAttack extends AbstractSimpleAttack<BerryBushAttack, GoldExperienceEntity> {
    private static final BlockState berryBush = Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 1);

    public BerryBushAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GoldExperienceEntity attacker, LivingEntity user, MoveContext ctx) {
        World world = attacker.getWorld();
        BlockPos blockPos = attacker.getBlockPos();
        if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.down()).isOpaque())
            world.setBlockState(blockPos, berryBush);

        return super.perform(attacker, user, ctx);
    }

    @Override
    protected @NonNull BerryBushAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BerryBushAttack copy() {
        return copyExtras(new BerryBushAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
