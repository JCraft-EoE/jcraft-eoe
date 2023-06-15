package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.world.World;

public class GEFrogEntity extends FrogEntity implements IOwnable {
    public GEFrogEntity(EntityType<? extends AnimalEntity> entityType, World world) { super(entityType, world); }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof LivingEntity living) {
            setAttacker(living);
            return living.damage(source, amount);
        }
        if (source.isOutOfWorld())
            return super.damage(source, amount);
        return false;
    }

    private LivingEntity master;
    @Override
    public LivingEntity getMaster() { return master; }
    @Override
    public void setMaster(LivingEntity m) { master = m; }

    private int timeToLive = 300;
    @Override
    public void tick() {
        timeToLive--;
        if (timeToLive == 0) {
            discard();
            dropStack(getMainHandStack());
        }

        if (!world.isClient) {
            if (master == null) {
                kill();
            } else {
                // Covers any edge cases, including stand damage (which uses a separate damage routine
                float deltaHealth = getMaxHealth() - getHealth();
                if (deltaHealth > 0) {
                    //JCraft.LOGGER.info("Redirecting " + deltaHealth + " damage from frog to " + master);
                    setHealth(getMaxHealth());
                    DamageSource source = getAttacker() == null ? DamageSource.GENERIC : DamageSource.mob(getAttacker());
                    master.damage(source, deltaHealth);
                }

                // Go to master
                getNavigation().startMovingTo(master, 2.5);
            }
        }

        super.tick();
    }
}
