package net.arna.jcraft.common.item;

import net.arna.jcraft.registry.ModItemRegister;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SheathedAnubisItem extends Item {
    public int state = 0;

    public SheathedAnubisItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.of("§9The sword/stand named after the Egyptian god of death."));
        tooltip.add(Text.of("§eBloodthirsty. §9Fuels itself on any and all violence."));
        tooltip.add(Text.of("§9Operates on a charge system; §eglints §9when charged."));
        tooltip.add(Text.of("§9Can used to §eblock."));
        tooltip.add(Text.of("§eHard to get rid of."));
        tooltip.add(Text.of("§9Can §eunsheathed §9with §eCrouch + RMB."));
        tooltip.add(Text.of("§cCrouching attack - heavy damage, 1s stun"));
        tooltip.add(Text.of("§aStanding attack - medium damage, 0.25s stun, lunge"));
        tooltip.add(Text.of("§bJumping attack - medium damage, knockback"));

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            user.setCurrentHand(hand);
        } else {
            user.setStackInHand(hand, new ItemStack(ModItemRegister.ANUBIS));
        }
        ItemStack itemStack = user.getStackInHand(hand);

        return TypedActionResult.consume(itemStack);
    }
}
