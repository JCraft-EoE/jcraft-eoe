package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class GreenBabyItem extends Item {
    public GreenBabyItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Check for correct stand id
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient) {
            NbtCompound playerNbt = ((IEntityDataSaver) user).getPersistentData();
            int standID = playerNbt.getInt("StandID");

            if (standID == StandType.WHITE_SNAKE.getId()) {
                if (!user.isCreative()) {
                    itemStack.decrement(1);
                }
                playerNbt.putInt("StandID", StandType.C_MOON.getId());
            }
        }

        return TypedActionResult.consume(itemStack);
    }
}
