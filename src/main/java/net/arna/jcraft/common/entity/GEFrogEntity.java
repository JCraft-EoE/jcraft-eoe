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
        if (source.isOutOfWorld()) {
            dropStack(getMainHandStack());
            discard();
            return true;
        }

        if (source.getAttacker() instanceof LivingEntity living)
            return living.damage(source, amount);
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
        boolean server = !world.isClient;

        if (server) {
            if (master == null) kill();
            else {
                // Go to master
                getNavigation().startMovingTo(master, 3);
            }

            if (--timeToLive == 0)
                kill();
        }

        super.tick();
    }
}
