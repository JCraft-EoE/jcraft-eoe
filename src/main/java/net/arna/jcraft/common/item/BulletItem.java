package net.arna.jcraft.common.item;

import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BulletItem extends Item {
    public BulletItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound itemData = stack.getNbt();

        if (itemData != null && itemData.contains("Caliber"))
            tooltip.add(Text.translatable("jcraft.bullet.caliber").append(" §e" + itemData.getFloat("Caliber") + "§9mm"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static ItemStack ofCaliber(float caliber) {
        ItemStack s = new ItemStack(JObjectRegistry.BULLET);
        NbtCompound nbt = s.getOrCreateNbt();
        nbt.putFloat("Caliber", caliber);
        return s;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack s = new ItemStack(this);
        NbtCompound nbt = s.getOrCreateNbt();
        nbt.putFloat("Caliber", 9);
        return s;
    }
}
