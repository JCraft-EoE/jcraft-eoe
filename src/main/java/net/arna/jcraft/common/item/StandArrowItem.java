package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.Random;

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
            NbtCompound playerData = ((IEntityDataSaver) user).getPersistentData();
            Random rand = new Random();
            int oldID = playerData.getInt("StandID");
            int newID = rand.nextInt(1, JCraft.STAND_COUNT + 1);
            while (newID == oldID) {
                //JCraft.LOGGER.info("NEW ID MATCHED OLD: " + newID + " " + oldID);
                newID = rand.nextInt(1, JCraft.STAND_COUNT + 1);
            }
            playerData.putInt("StandID", newID);
            user.detach();
            JCraft.summon(world, user);
        }

        return TypedActionResult.consume(itemStack);
    }
}
