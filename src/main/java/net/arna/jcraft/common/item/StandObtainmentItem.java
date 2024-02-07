package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
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
    protected final Map<StandType, StandType> standIOMap = new HashMap<>();

    protected boolean canEvolve(World world, PlayerEntity user) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.consume(itemStack);

        StandComponent standData = JComponents.getStandData(user);
        StandType type = standData.getType();

        // Does the user have the appropriate stand and does he meet the evolution requirements?
        if (standIOMap.containsKey(type) && canEvolve(world, user)) {
            if (!user.isCreative())
                itemStack.decrement(1);

            standData.setType(standIOMap.get(type));

            // Re-summon users stand
            StandEntity<?, ?> stand = standData.getStand();
            if (stand != null) stand.desummon();
            JCraft.summon(world, user);
        }

        return TypedActionResult.consume(itemStack);
    }
}
