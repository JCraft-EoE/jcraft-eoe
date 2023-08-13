package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GreenBabyItem extends StandObtainmentItem {
    public GreenBabyItem(Settings settings) {
        super(settings);

        standIOMap.put(StandType.WHITE_SNAKE, StandType.C_MOON);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.greenbaby.desc"));
        tooltip.add(Text.translatable("jcraft.greenbaby.evodesc"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
