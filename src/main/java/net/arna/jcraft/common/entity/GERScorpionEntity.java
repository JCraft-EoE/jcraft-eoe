package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public class GERScorpionEntity extends MobEntity implements IAnimatable, IAnimationTickable { //todo: implement IOwnable
    public GERScorpionEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoDrag(true);
    }

    public void setInitialVel(Vec3d initV) {
        this.setVelocity(initV);
        initialVel = initV;
    }

    private Vec3d initialVel;
    private LivingEntity jumpTarget;
    private LivingEntity owner;
    private int landedTimer;
    private static final TrackedData<Optional<UUID>> OWNERUUID;
    private static final TrackedData<Boolean> ISROCK;
    private static final TrackedData<Boolean> CHARGED;

    static {
        OWNERUUID = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        ISROCK = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CHARGED = DataTracker.registerData(GERScorpionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public LivingEntity getOwner() {
        return this.owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.setOwnerUUID(owner.getUuid());
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
        if (nbt.contains("OwnerUUID")) {
            this.setOwnerUUID(nbt.getUuid("OwnerUUID"));
        }
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

        if (world.isClient) {
            if (!isRock()) landedTimer += 1;
            double x = getX();
            double y = getY();
            double z = getZ();
            if (landedTimer < 1) { // Laser
                Vec3d towardsVec = JUtils.deltaPos(this);
                for (double i = 0; i < 6; i++) {
                    double lerp = i / 6;
                    world.addParticle(
                            isCharged() ? ParticleTypes.WITCH : ParticleTypes.COMPOSTER
                            , x + towardsVec.x * lerp, y + towardsVec.y * lerp, z + towardsVec.z * lerp
                            , towardsVec.x, towardsVec.y, towardsVec.z);
                }
            } else if (landedTimer == 1) { // Landing burst
                for (int i = 0; i < 8; i++) {
                    world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                            x + random.nextFloat() - 0.5f,
                            y + random.nextFloat() - 0.5f,
                            z + random.nextFloat() - 0.5f,
                            0, 0, 0
                    );
                }
            }
        } else {
            if (owner != null) {
                List<Entity> filter = new ArrayList<>();
                filter.add(owner);
                filter.add(this);
                if (owner.hasPassengers()) {
                    filter.addAll(owner.getPassengerList());
                }
                if (isRock()) {
                    if (!getVelocity().equals(initialVel)) // Ghetto collision check
                        Transform();

                    // Recursive hitbox check between current and previous position
                    Vec3d towardsVec = curPos.subtract(new Vec3d(prevX, prevY, prevZ));
                    List<LivingEntity> hurtAll = new ArrayList<>();
                    for (double i = 0; i < 3; i++) {
                        List<LivingEntity> hurt = JUtils.generateHitbox(world, curPos.add(towardsVec.multiply(i / 3)), 0.5, filter);
                        hurt.removeIf(hurtAll::contains);
                        hurtAll.addAll(hurt);
                    }

                    if (!hurtAll.isEmpty()) {
                        jumpTarget = hurtAll.get(0);
                        for (LivingEntity l :
                                hurtAll) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            damageLogic(world, target, getVelocity(), rockStun, 1, false, 6f, true, 10, DamageSource.mob(owner), owner);
                        }
                        Transform();
                        JCraft.CreateParticle((ServerWorld) this.world,
                                curPos.x + random.nextGaussian() * 0.25,
                                curPos.y + random.nextGaussian() * 0.25,
                                curPos.z + random.nextGaussian() * 0.25,
                                2);
                    }
                } else {
                    landedTimer += 1;
                    if (landedTimer == 15) { // Pounce at target
                        if (jumpTarget != null) {
                            Vec3d eyePos = jumpTarget.getPos().add(0, jumpTarget.getHeight() / 2, 0);
                            lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, eyePos);
                            setVelocity(
                                    getVelocity().add(
                                            eyePos.subtract(getPos()).multiply(0.33) // Non-normalized to account for distance
                                    )
                            );
                        } else {
                            addVelocity(0, 0.65, 0);
                        }
                        velocityModified = true;
                    }
                    if (landedTimer == 20) { // Sting followup, 5t gap
                        List<LivingEntity> hurt = JUtils.generateHitbox(world, getPos(), 1.5, filter);
                        if (isCharged())
                            for (LivingEntity l :
                                    hurt) {
                                LivingEntity target = JUtils.getUserIfStand(l);
                                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, false, true));
                                damageLogic(world, target, Vec3d.ZERO, 15, 1, false, 3f, true, 7, DamageSource.mob(owner), owner);
                            }
                        else
                            for (LivingEntity l :
                                    hurt) {
                                LivingEntity target = JUtils.getUserIfStand(l);
                                damageLogic(world, target, Vec3d.ZERO, 15, 1, false, 3f, true, 7, DamageSource.mob(owner), owner);
                            }
                    }
                }
                if (age > 30)
                    kill();
            } else if (getOwnerUUID().isPresent()) {
                UUID searchID = getOwnerUUID().get();
                Box box = Box.of(this.getPos(), 64, 64, 64);
                boolean found = false;

                for (LivingEntity e :
                        world.getEntitiesByClass(LivingEntity.class, box, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
                    if (e.getUuid().equals(searchID)) {
                        setOwner(e);
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
    final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.isRock())
            event.getController().setAnimation(new AnimationBuilder().loop("animation.gerscorpion.rock"));
        else
            event.getController().setAnimation(new AnimationBuilder().playOnce("animation.gerscorpion.transform").playAndHold("animation.gerscorpion.attack"));
        return PlayState.CONTINUE;
    }
}
