package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class StandArrowItem extends Item {
    public StandArrowItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Remove 1 from item stack
        ItemStack itemStack = user.getStackInHand(hand);
        if (!user.isCreative()) {
            itemStack.decrement(1);
        }

        // 1 second usage cooldown to prevent overuse
        user.getItemCooldownManager().set(this, 20);

        // Roll for stand (can't roll the same one twice)
        if (!world.isClient) {
            StandComponent standData = JComponents.getStandData(user);

            Random random = Random.create();
            StandType oldType = standData.getType();
            StandType newType;
            do {
                newType = StandType.getRandomRegular(random);
            } while (newType == oldType);

            standData.setType(newType);
            user.detach();
            JCraft.summon(world, user);

            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }

        return TypedActionResult.consume(itemStack);
    }
}
