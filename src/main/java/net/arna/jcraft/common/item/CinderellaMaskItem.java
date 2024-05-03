package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CinderellaMaskItem extends Item {
    public CinderellaMaskItem() {
        super(new Settings()
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.cinderella_mask.enchdesc"));
        tooltip.add(Text.translatable("jcraft.cinderella_mask.usedesc"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
