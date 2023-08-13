package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DIOsDiaryItem extends StandObtainmentItem {
    public DIOsDiaryItem(Settings settings) {
        super(settings);

        standIOMap.put(StandType.C_MOON, StandType.MADE_IN_HEAVEN);
        standIOMap.put(StandType.THE_WORLD, StandType.THE_WORLD_OVER_HEAVEN);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.diosdiary.desc"));
        tooltip.add(Text.translatable("jcraft.diosdiary.evodesc"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
