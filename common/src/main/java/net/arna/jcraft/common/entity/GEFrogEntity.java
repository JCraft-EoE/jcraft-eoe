package net.arna.jcraft.common.entity;

import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.itfs.ICustomDamageHandler;
import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class GEFrogEntity extends Frog implements IOwnable, ICustomDamageHandler {
    public GEFrogEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            spawnAtLocation(getMainHandItem());
            discard();
            return true;
        }

        if (source.getEntity() instanceof LivingEntity living) {
            return living.hurt(source, amount);
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
        boolean server = !level().isClientSide;

        if (server) {
            if (master == null) {
                kill();
            } else {
                // Go to master
                getNavigation().moveTo(master, 3);
            }

            if (--timeToLive < 1) {
                kill();
            }
        }

        super.tick();
    }

    @Override
    public boolean reflectsDamage() {
        return true;
    }

    @Override
    public boolean handleDamage(Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage,
                                boolean lift, int blockstun, DamageSource source, Entity attacker, CommonHitPropertyComponent.HitAnimation hitAnimation,
                                MoveUsage moveUsage, boolean canBackstab, boolean unblockable) {
        if (attacker instanceof LivingEntity living) {
            damageLogic(
                    attacker.level(),
                    living,
                    new AttackData(kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, moveUsage, canBackstab, unblockable)
            );
        }
        return false;
    }
}
