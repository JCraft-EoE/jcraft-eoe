package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class LifeDetector extends LivingEntity implements IAnimatable {
    public LivingEntity target;
    private LivingEntity owner;
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public static TrackedData<Integer> STATE;
    static { STATE = DataTracker.registerData(LifeDetector.class, TrackedDataHandlerRegistry.INTEGER); }
    public int getState() { return this.dataTracker.get(STATE); }
    public void setState(int s) { this.dataTracker.set(STATE, s); }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(STATE, 0);
    }

    public LifeDetector(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    public void setOwner(LivingEntity l) { this.owner = l; }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target == this) return false;
        if (target == owner) return false;
        if (target.isConnectedThroughVehicle(owner)) return false;
        return target.canTakeDamage();
    }

    private void Explode() {
        Vec3d pos = getPos();
        List<LivingEntity> hurt = JCraftUtils.GenerateHitbox(world, pos, 2, null);
        for (LivingEntity living :
                hurt) {
            if (living == owner || living.getVehicle() == owner) continue;
            Vec3d kbVec = living.getPos().subtract(pos).normalize();
            StandEntity.damageLogic(world, living, kbVec, 10, 1, false, 5f, true, DamageSource.mob(owner), owner);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) return;

        if (world.isClient) {
            this.world.addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + random.nextFloat() - 0.5f,
                    this.getY() + random.nextFloat() - 0.5f,
                    this.getZ() + random.nextFloat() - 0.5f,
                    0.0, 0.0, 0.0
            );
        } else {
            if (target == null) {
                if (this.age % 2 == 0) {
                    List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(32f), EntityPredicates.VALID_ENTITY);

                    for (LivingEntity t :
                            targets) {
                        if (!canTarget(t)) continue;
                        target = t;
                        break;
                    }
                }
            } else {
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, target.getEyePos());
                if (this.squaredDistanceTo(target) < 2 || this.age >= 300) Explode(); //If closer than 1.41m or 15s old
            }

            this.setVelocity(this.getRotationVector().multiply(0.25f));
            this.velocityModified = true;
        }
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() { return List.of(); }
    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) { }
    @Override
    public Arm getMainArm() { return null; }
    @Override
    public void registerControllers(AnimationData animationData) {  }
    @Override
    public AnimationFactory getFactory() { return this.factory; }
}
