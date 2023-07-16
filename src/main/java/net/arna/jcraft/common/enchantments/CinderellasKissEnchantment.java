package net.arna.jcraft.common.enchantments;

import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class CinderellasKissEnchantment extends Enchantment {
    public static final CinderellasKissEnchantment INSTANCE = new CinderellasKissEnchantment();
    
    private CinderellasKissEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEARABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() == JObjectRegistry.CINDERELLA_MASK;
    }
    
    public static int getCKLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(INSTANCE, stack);
    }
}
