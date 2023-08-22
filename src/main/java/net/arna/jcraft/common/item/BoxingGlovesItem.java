package net.arna.jcraft.common.item;

import net.arna.jcraft.common.spec.SpecType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoxingGlovesItem extends SpecObtainmentItem {
    public BoxingGlovesItem(Settings settings, SpecType spec) {
        super(settings, spec);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.boxinggloves.desc"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            boolean specChanged = tryGetSpec(user);
            if (specChanged) {
                if (!user.isCreative()) itemStack.decrement(1);
                user.getItemCooldownManager().set(this, 20);
            }
        }

        return TypedActionResult.consume(itemStack);
    }
}
