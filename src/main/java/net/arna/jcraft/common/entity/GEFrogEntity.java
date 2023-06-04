package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.world.World;

public class GEFrogEntity extends FrogEntity implements IOwnable {

    public GEFrogEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof LivingEntity living)
            return living.damage(source, amount);
        if (source.isOutOfWorld())
            return super.damage(source, amount);
        return false;
    }

    private LivingEntity master;
    @Override
    public LivingEntity getMaster() {
        return master;
    }
    @Override
    public void setMaster(LivingEntity m) {
        master = m;
    }

    private int timeToLive = 300;
    @Override
    public void tick() {
        super.tick();
        if (world.isClient) return;
        timeToLive--;
        getNavigation().startMovingTo(master, 2);
        if (timeToLive == 0) {
            kill();
            dropStack(getMainHandStack());
        }
    }
}
