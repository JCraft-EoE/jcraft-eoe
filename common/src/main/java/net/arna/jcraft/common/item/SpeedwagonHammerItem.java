package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JBlockRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class SpeedwagonHammerItem extends DiggerItem {
    public SpeedwagonHammerItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(attackDamageModifier, attackSpeedModifier, tier, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(JBlockRegistry.METEORITE_BLOCK.get()) ||
                state.is(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get()) ||
                super.isCorrectToolForDrops(state);
    }
}