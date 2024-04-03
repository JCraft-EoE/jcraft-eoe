package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.ICustomDamageHandler;
import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;

public class GEFrogEntity extends FrogEntity implements IOwnable, ICustomDamageHandler {
    public GEFrogEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(DamageTypes.OUT_OF_WORLD)) {
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
        boolean server = !getWorld().isClient;

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

    @Override
    public boolean reflectsDamage() {
        return true;
    }

    @Override
    public boolean handleDamage(Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        if (attacker instanceof LivingEntity living)
            damageLogic(attacker.getWorld(), living, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, canBackstab, unblockable);
        return false;
    }
}
