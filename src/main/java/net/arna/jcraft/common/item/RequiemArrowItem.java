package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RequiemArrowItem extends Item {
    public RequiemArrowItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.requiemarrow.desc"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Check for correct stand id
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient) {
            NbtCompound playerNbt = ((IEntityDataSaver) user).getPersistentData();
            int standID = playerNbt.getInt("StandID");
            if (standID == StandType.GOLD_EXPERIENCE.getId()) {
                if (!user.isCreative()) {
                    itemStack.decrement(1);
                }
                playerNbt.putInt("StandID", StandType.GOLD_EXPERIENCE_REQUIEM.getId());
                if (!user.isCreative()) {
                    itemStack.decrement(1);
                }
            }
        }

        return TypedActionResult.consume(itemStack);
    }
}
