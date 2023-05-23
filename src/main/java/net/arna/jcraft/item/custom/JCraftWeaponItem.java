package net.arna.jcraft.item.custom;

import net.arna.jcraft.util.Attack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class JCraftWeaponItem extends Item {
    public int moveStun = 0;
    public Attack attack;

    public JCraftWeaponItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
    }
}
