package net.arna.jcraft.common.entity;

import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.arna.jcraft.api.Attacks.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class GERScorpionEntity extends Mob implements IOwnable {
    private static final EntityDataAccessor<Optional<UUID>> OWNERUUID;
    private static final EntityDataAccessor<Boolean> ISROCK;
    private static final EntityDataAccessor<Boolean> CHARGED;
    private Vec3 initialVel;
    private LivingEntity jumpTarget;
    private LivingEntity owner;
    private int landedTimer;

    static {
        OWNERUUID = SynchedEntityData.defineId(GERScorpionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
        ISROCK = SynchedEntityData.defineId(GERScorpionEntity.class, EntityDataSerializers.BOOLEAN);
        CHARGED = SynchedEntityData.defineId(GERScorpionEntity.class, EntityDataSerializers.BOOLEAN);
    }

    public GERScorpionEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
        this.setDiscardFriction(true);
    }

    public void setInitialVel(Vec3 initV) {
        this.setDeltaMovement(initV);
        initialVel = initV;
        ROCK_IDLE.sendForEntity(this);
    }

    public Optional<UUID> getOwnerUUID() {
        return entityData.get(OWNERUUID);
    }

    public void setOwnerUUID(UUID uuid) {
        entityData.set(OWNERUUID, Optional.of(uuid));
    }

    public boolean isRock() {
        return entityData.get(ISROCK);
    }

    public void setRock(boolean r) {
        entityData.set(ISROCK, r);
    }

    public boolean isCharged() {
        return entityData.get(CHARGED);
    }

    private int rockStun = 15;

    public void charge() {
        entityData.set(CHARGED, true);
        rockStun = 21;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNERUUID, Optional.empty());
        entityData.define(ISROCK, true);
        entityData.define(CHARGED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        Optional<UUID> ownerID = this.getOwnerUUID();
        ownerID.ifPresent(id -> nbt.putUUID("OwnerUUID", id));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("OwnerUUID")) {
            setOwnerUUID(nbt.getUUID("OwnerUUID"));
        }
    }

    @Override
    public LivingEntity getMaster() {
        return owner;
    }

    @Override
    public void setMaster(LivingEntity entity) {
        owner = entity;
        setOwnerUUID(owner.getUUID());
    }

    // Scorpions aren't very heavy
    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    // Ease of use
    @Override
    public boolean isNoGravity() {
        if (isRock()) {
            return true;
        }
        return super.isNoGravity();
    }

    private void Transform() {
        setDeltaMovement(Vec3.ZERO);
        hurtMarked = true;
        setDiscardFriction(false);
        setRock(false);
        TRANSFORM.sendForEntity(this);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 curPos = position();

        if (level().isClientSide) {
            if (!isRock()) {
                landedTimer += 1;
            }
            double x = getX();
            double y = getY();
            double z = getZ();
            if (landedTimer < 1) { // Laser
                Vec3 towardsVec = JUtils.deltaPos(this);
                for (double i = 0; i < 6; i++) {
                    double lerp = i / 6;
                    level().addParticle(
                            isCharged() ? ParticleTypes.WITCH : ParticleTypes.COMPOSTER,
                            x + towardsVec.x * lerp, y + towardsVec.y * lerp, z + towardsVec.z * lerp,
                            towardsVec.x, towardsVec.y, towardsVec.z);
                }
            } else if (landedTimer == 1) { // Landing burst
                for (int i = 0; i < 8; i++) {
                    level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
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
                if (owner.isVehicle()) {
                    filter.addAll(owner.getPassengers());
                }
                DamageSource damageSource = level().damageSources().mobAttack(owner);
                if (isRock()) {
                    if (!getDeltaMovement().equals(initialVel)) // Ghetto collision check
                    {
                        Transform();
                    }

                    // Recursive hitbox check between current and previous position
                    Vec3 towardsVec = curPos.subtract(new Vec3(xo, yo, zo));
                    List<LivingEntity> hurtAll = new ArrayList<>();
                    for (double i = 0; i < 3; i++) {
                        hurtAll.addAll(JUtils.generateHitbox(level(), curPos.add(towardsVec.scale(i / 3)), 0.5, filter));
                    }

                    hurtAll.removeIf(e -> !canDamage(damageSource, e));

                    if (!hurtAll.isEmpty()) {
                        jumpTarget = hurtAll.get(0);
                        for (LivingEntity l : hurtAll) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            damageLogic(level(), target, getDeltaMovement(), rockStun, 1, false, 6f,
                                    true, 10, damageSource, owner, CommonHitPropertyComponent.HitAnimation.MID);
                        }
                        Transform();
                        JCraft.createParticle((ServerLevel) this.level(),
                                curPos.x + random.nextGaussian() * 0.25,
                                curPos.y + random.nextGaussian() * 0.25,
                                curPos.z + random.nextGaussian() * 0.25,
                                JParticleType.HIT_SPARK_1);
                    }
                } else {
                    landedTimer += 1;
                    if (landedTimer == 15) { // Pounce at target
                        if (jumpTarget != null) {
                            Vec3 eyePos = jumpTarget.position().add(0, jumpTarget.getBbHeight() / 2, 0);
                            lookAt(EntityAnchorArgument.Anchor.EYES, eyePos);
                            setDeltaMovement(getDeltaMovement().add(eyePos.subtract(position()).scale(0.33))); // Non-normalized to account for distance
                        } else {
                            push(0, 0.65, 0);
                        }

                        hurtMarked = true;
                        ATTACK.sendForEntity(this);
                    }

                    if (landedTimer == 20) { // Sting followup, 5t gap
                        Set<LivingEntity> hurt = JUtils.generateHitbox(level(), position(), 1.5, filter);
                        if (isCharged()) {
                            for (LivingEntity l : hurt) {
                                LivingEntity target = JUtils.getUserIfStand(l);
                                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
                                damageLogic(level(), target, Vec3.ZERO, 15, 1, false, 3f, true, 7, damageSource, owner, CommonHitPropertyComponent.HitAnimation.MID);
                            }
                        } else {
                            for (LivingEntity l : hurt) {
                                LivingEntity target = JUtils.getUserIfStand(l);
                                damageLogic(level(), target, Vec3.ZERO, 15, 1, false, 3f, true, 7, damageSource, owner, CommonHitPropertyComponent.HitAnimation.MID);
                            }
                        }
                    }
                }

                if (tickCount > 30) {
                    kill();
                }
            } else if (getOwnerUUID().isPresent()) {
                UUID searchID = getOwnerUUID().get();
                AABB box = AABB.ofSize(this.position(), 64, 64, 64);
                boolean found = false;

                for (LivingEntity e :
                        level().getEntitiesOfClass(LivingEntity.class, box, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                    if (e.getUUID().equals(searchID)) {
                        setMaster(e);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    kill();
                }
            }
        }
    }

    private static final AzCommand ROCK_IDLE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.gerscorpion.rock", AzPlayBehaviors.LOOP);
    private static final AzCommand TRANSFORM = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.gerscorpion.transform");
    private static final AzCommand ATTACK = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.gerscorpion.attack", AzPlayBehaviors.HOLD_ON_LAST_FRAME);
}
