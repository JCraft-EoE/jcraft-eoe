package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
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
            discard();
            return true;
        }

        if (source.getAttacker() instanceof LivingEntity living) {
            setAttacker(living);
            return living.damage(source, amount);
        }

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
                // Covers any edge cases, including stand damage (which uses a separate damage routine
                float deltaHealth = getMaxHealth() - getHealth();
                if (deltaHealth > 0) {
                    setHealth(getMaxHealth());

                    LivingEntity attacker = getAttacker();
                    if (attacker == null) attacker = getDamageTracker().getBiggestAttacker();
                    attacker = JUtils.getUserIfStand(attacker);
                    if (attacker != null) attacker.damage(DamageSource.mob(attacker), deltaHealth);
                }

                // Go to master
                getNavigation().startMovingTo(master, 3);
            }

            if (--timeToLive == 0) {
                dropStack(getMainHandStack());
                kill();
            }
        }

        super.tick();
    }
}
