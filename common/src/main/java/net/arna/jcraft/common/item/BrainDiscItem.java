package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.CommonStandComponent;
import net.arna.jcraft.common.entity.BrainType;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.registry.JItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrainDiscItem extends Item {

    private static final String BRAIN_ID_STR = "BrainID";

    public BrainDiscItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        BrainType type = getBrainType(stack);
        if (type == null) {
            tooltip.add(Component.literal("Empty").withStyle(s -> s.applyFormat(ChatFormatting.GRAY)));
            return;
        }

        tooltip.add(type.getName().copy().withStyle(s -> s.withColor(ChatFormatting.GRAY)));
    }

    public static ItemStack createDiscStack(BrainType type) {
        ItemStack stack = new ItemStack(JItemRegistry.BRAIN_DISC.get());
        CompoundTag nbt = stack.getOrCreateTag();
        if (type != null) {
            nbt.putInt(BRAIN_ID_STR, type.ordinal());
        }
        return stack;
    }

    public static boolean isEmptyDisc(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().contains(BRAIN_ID_STR, Tag.TAG_INT);
    }

    public static BrainType getBrainType(ItemStack stack) {
        if (!stack.is(JItemRegistry.BRAIN_DISC.get())) {
            return null;
        }
        CompoundTag nbt = stack.getTag();
        return nbt == null || !nbt.contains(BRAIN_ID_STR, Tag.TAG_INT) ? null : BrainType.values()[nbt.getInt(BRAIN_ID_STR)];
    }
}
