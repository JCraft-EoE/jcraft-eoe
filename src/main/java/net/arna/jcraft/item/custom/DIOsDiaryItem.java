package net.arna.jcraft.item.custom;

import net.arna.jcraft.util.IEntityDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class DIOsDiaryItem extends Item {
    public DIOsDiaryItem(Settings settings) {
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

            if (standID == -1) {
                if (!user.isCreative()) { itemStack.decrement(1); }
                playerNbt.putInt("StandID", -2);
            }

            if (standID == 2) {
                if (!user.isCreative()) { itemStack.decrement(1); }
                playerNbt.putInt("StandID", -3);
            }
        }

        return TypedActionResult.consume(itemStack);
    }
}
