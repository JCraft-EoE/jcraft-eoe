package net.arna.jcraft.common.item;

import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ISpec;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class BoxingGlovesItem extends Item {
    public BoxingGlovesItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (!user.isCreative()) itemStack.decrement(1);

            user.getItemCooldownManager().set(this, 20);

            NbtCompound data = ((IEntityDataSaver) user).getPersistentData();
            data.putInt("SpecID", 1);
            JCraftUtils.assignSpec(user, data, (ISpec)user);
        }

        return TypedActionResult.consume(itemStack);
    }
}
