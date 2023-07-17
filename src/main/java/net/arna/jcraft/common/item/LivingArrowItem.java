package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.StandType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LivingArrowItem extends StandObtainmentItem {
    public LivingArrowItem(Settings settings) {
        super(settings);

        standIOMap = Map.ofEntries(
                Map.entry(StandType.KILLER_QUEEN.getId(), StandType.KILLER_QUEEN_BITES_THE_DUST.getId()),
                Map.entry(StandType.STAR_PLATINUM.getId(), StandType.STAR_PLATINUM_THE_WORLD.getId())
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.livingarrow.desc"));
        tooltip.add(Text.translatable("jcraft.livingarrow.evodesc"));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
