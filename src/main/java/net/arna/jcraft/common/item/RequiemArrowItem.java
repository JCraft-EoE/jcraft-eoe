package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RequiemArrowItem extends StandObtainmentItem {
    public RequiemArrowItem(Settings settings) {
        super(settings);
        standIOMap.put(StandType.GOLD_EXPERIENCE, StandType.GOLD_EXPERIENCE_REQUIEM);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.requiemarrow.desc"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
