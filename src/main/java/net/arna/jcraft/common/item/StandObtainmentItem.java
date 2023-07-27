package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Map;

public abstract class StandObtainmentItem extends Item {
    public StandObtainmentItem(Settings settings) {
        super(settings);
    }

    /**
     * List of input/output stand IDs required by the StandObtainmentItem instance
     * Key - input
     * Value - output
     */
    public Map<Integer, Integer> standIOMap;

    protected boolean canEvolve(World world, PlayerEntity user, NbtCompound playerData) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.consume(itemStack);

        IEntityDataSaver userDataSaver = ((IEntityDataSaver) user);
        NbtCompound playerData = userDataSaver.getPersistentData();
        int standID = playerData.getInt("StandID");

        // Does the user have the appropriate stand and does he meet the evolution requirements?
        if (standIOMap.containsKey(standID) && canEvolve(world, user, playerData)) {
            if (!user.isCreative())
                itemStack.decrement(1);

            playerData.putInt("StandID", standIOMap.get(standID));

            // Re-summon users stand
            StandEntity<?, ?> stand = userDataSaver.getStand();
            if (stand != null)
                stand.desummon();

            JCraft.summon(world, user);
        }

        return TypedActionResult.consume(itemStack);
    }
}
