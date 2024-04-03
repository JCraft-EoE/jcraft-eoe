package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

import static net.arna.jcraft.common.util.JUtils.canDamage;
import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;

public class GERScorpionEntity extends MobEntity implements GeoEntity, IOwnable {
    private static final TrackedData<Optional<UUID>> OWNERUUID;
    private static final TrackedData<Boolean> ISROCK;
    private static final TrackedData<Boolean> CHARGED;
    private Vec3d initialVel;
    private LivingEntity jumpTarget;
    private LivingEntity owner;
    private int landedTimer;

    static {
        OWNERUUID = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        ISROCK = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CHARGED = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public GERScorpionEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoDrag(true);
    }

    public void setInitialVel(Vec3d initV) {
        this.setVelocity(initV);
        initialVel = initV;
    }

    public Optional<UUID> getOwnerUUID() {
        return dataTracker.get(OWNERUUID);
    }

    public void setOwnerUUID(UUID uuid) {
        dataTracker.set(OWNERUUID, Optional.of(uuid));
    }

    public boolean isRock() {
        return dataTracker.get(ISROCK);
    }

    public void setRock(boolean r) {
        dataTracker.set(ISROCK, r);
    }

    public boolean isCharged() {
        return dataTracker.get(CHARGED);
    }

    private int rockStun = 15;

    public void charge() {
        dataTracker.set(CHARGED, true);
        rockStun = 21;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OWNERUUID, Optional.empty());
        dataTracker.startTracking(ISROCK, true);
        dataTracker.startTracking(CHARGED, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        Optional<UUID> ownerID = this.getOwnerUUID();
        ownerID.ifPresent(id -> nbt.putUuid("OwnerUUID", id));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("OwnerUUID")) setOwnerUUID(nbt.getUuid("OwnerUUID"));
    }

    @Override
    public LivingEntity getMaster() {
        return owner;
    }

    @Override
    public void setMaster(LivingEntity entity) {
        owner = entity;
        setOwnerUUID(owner.getUuid());
    }

    // Scorpions aren't very heavy
    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    // Ease of use
    @Override
    public boolean hasNoGravity() {
        if (isRock())
            return true;
        return super.hasNoGravity();
    }

    private void Transform() {
        setVelocity(Vec3d.ZERO);
        velocityModified = true;
        setNoDrag(false);
        setRock(false);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d curPos = getPos();

        if (getWorld().isClient) {
            if (!isRock()) landedTimer += 1;
            double x = getX();
            double y = getY();
            double z = getZ();
            if (landedTimer < 1) { // Laser
                Vec3d towardsVec = JUtils.deltaPos(this);
                for (double i = 0; i < 6; i++) {
                    double lerp = i / 6;
                    getWorld().addParticle(
                            isCharged() ? ParticleTypes.WITCH : ParticleTypes.COMPOSTER,
                            x + towardsVec.x * lerp, y + towardsVec.y * lerp, z + towardsVec.z * lerp,
                            towardsVec.x, towardsVec.y, towardsVec.z);
                }
            } else if (landedTimer == 1) { // Landing burst
                for (int i = 0; i < 8; i++) {
                    getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                            x + random.nextFloat() - 0.5f,
                            y + random.nextFloat() - 0.5f,
                            z + random.nextFloat() - 0.5f,
                            0, 0, 0
                    );
                }
            }
        } else {
            if (owner != null) {
                Set<Entity> filter = new HashSet<>();
                filter.add(owner);
                filter.add(this);
                if (owner.hasPassengers()) filter.addAll(owner.getPassengerList());
                DamageSource damageSource = getWorld().getDamageSources().mobAttack(owner);
                if (isRock()) {
                    if (!getVelocity().equals(initialVel)) // Ghetto collision check
                        Transform();

                    // Recursive hitbox check between current and previous position
                    Vec3d towardsVec = curPos.subtract(new Vec3d(prevX, prevY, prevZ));
                    List<LivingEntity> hurtAll = new ArrayList<>();
                    for (double i = 0; i < 3; i++)
                        hurtAll.addAll(JUtils.generateHitbox(getWorld(), curPos.add(towardsVec.multiply(i / 3)), 0.5, filter));

                    hurtAll.removeIf(e -> !canDamage(damageSource, e));

                    if (!hurtAll.isEmpty()) {
                        jumpTarget = hurtAll.get(0);
                        for (LivingEntity l : hurtAll) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            damageLogic(getWorld(), target, getVelocity(), rockStun, 1, false, 6f,
                                    true, 10, damageSource, owner, HitPropertyComponent.HitAnimation.MID);
                        }
                        Transform();
                        JCraft.createParticle((ServerWorld) this.getWorld(),
                                curPos.x + random.nextGaussian() * 0.25,
                                curPos.y + random.nextGaussian() * 0.25,
                                curPos.z + random.nextGaussian() * 0.25,
                                JParticleType.HIT_SPARK_1);
                    }
                } else {
                    landedTimer += 1;
                    if (landedTimer == 15) { // Pounce at target
                        if (jumpTarget != null) {
                            Vec3d eyePos = jumpTarget.getPos().add(0, jumpTarget.getHeight() / 2, 0);
                            lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, eyePos);
                            setVelocity(getVelocity().add(eyePos.subtract(getPos()).multiply(0.33))); // Non-normalized to account for distance
                        } else addVelocity(0, 0.65, 0);
                        velocityModified = true;
                    }

                    if (landedTimer == 20) { // Sting followup, 5t gap
                        Set<LivingEntity> hurt = JUtils.generateHitbox(getWorld(), getPos(), 1.5, filter);
                        if (isCharged()) for (LivingEntity l : hurt) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, false, true));
                            damageLogic(getWorld(), target, Vec3d.ZERO, 15, 1, false, 3f, true, 7, damageSource, owner, HitPropertyComponent.HitAnimation.MID);
                        }
                        else for (LivingEntity l : hurt) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            damageLogic(getWorld(), target, Vec3d.ZERO, 15, 1, false, 3f, true, 7, damageSource, owner, HitPropertyComponent.HitAnimation.MID);
                        }
                    }
                }

                if (age > 30) kill();
            } else if (getOwnerUUID().isPresent()) {
                UUID searchID = getOwnerUUID().get();
                Box box = Box.of(this.getPos(), 64, 64, 64);
                boolean found = false;

                for (LivingEntity e :
                        getWorld().getEntitiesByClass(LivingEntity.class, box, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
                    if (e.getUuid().equals(searchID)) {
                        setMaster(e);
                        found = true;
                        break;
                    }
                }

                if (!found)
                    kill();
            }
        }
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GERScorpionEntity> state) {
        if (this.isRock())
            state.setAnimation(RawAnimation.begin().thenLoop("animation.gerscorpion.rock"));
        else
            state.setAnimation(RawAnimation.begin().thenPlay("animation.gerscorpion.transform").thenPlayAndHold("animation.gerscorpion.attack"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
