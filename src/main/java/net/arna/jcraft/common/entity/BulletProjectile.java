package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.item.BulletItem;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BulletProjectile extends PersistentProjectileEntity {
    private float caliber = 9f; //mm
    private int stunTicks = 0;
    private int damage;

    //62.5 mm/px

    public BulletProjectile(EntityType<? extends BulletProjectile> entityType, World world) {
        super(entityType, world);
    }

    public BulletProjectile(World world, LivingEntity owner, float caliber, int stunTicks, int damage) {
        super(JEntityTypeRegister.BULLET, owner, world);

        this.caliber = caliber;
        this.stunTicks = stunTicks;
        this.damage = damage;
    }

    @Override
    protected ItemStack asItemStack() {
        return BulletItem.ofCaliber(caliber);
    }
}
