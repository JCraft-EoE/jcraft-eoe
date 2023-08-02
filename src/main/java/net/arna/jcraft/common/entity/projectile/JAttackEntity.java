package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.world.World;

import java.util.List;

public class JAttackEntity extends LivingEntity implements IOwnable {
    protected LivingEntity master;

    protected JAttackEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public LivingEntity getMaster() {
        return this.master;
    }

    @Override
    public void setMaster(LivingEntity m) {
        this.master = m;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {

    }

    @Override
    public Arm getMainArm() {
        return null;
    }
}
