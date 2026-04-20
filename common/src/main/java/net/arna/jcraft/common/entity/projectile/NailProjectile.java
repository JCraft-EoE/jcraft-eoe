package net.arna.jcraft.common.entity.projectile;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.projectile.JAttackEntity;
import net.arna.jcraft.common.entity.stand.TuskAct1Entity;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NailProjectile extends AbstractArrow implements IOwnable {
    public static final float NAIL_COST = 1.0f;

    @Getter
    @Setter
    private LivingEntity master;

    private int ticksInAir;
    @Setter
    private boolean spinning = false;
    private boolean wormhole = false;
    @Setter
    private int chargeTime = 0;
    private boolean homing = false;
    private boolean penetrating = false;
    private float customDamage = 4.0f;

    // Made public so NailShotAttack can access it
    public float maxRange = 100.0f;
    public float creepDistance = 0.0f;

    private boolean isDrilling = false;
    private int lastHitTick = -1;
    private boolean wasInBlock = false;
    /** Velocity-scaled damage drill: damage = min(4, speed), slows on hit, noPhysics above speed 2.0 */
    public boolean velocityDrilling = false;
    /** Fan nail: flies straight until first entity hit, then homes toward nearest entity */
    public boolean fanNail = false;
    /** Fan nail: gently homes toward nearest entity, manual hit detection */
    public boolean homingFan = false;
    private final Map<UUID, Integer> drillingEntityLastHit = new java.util.HashMap<>();
    private final Map<UUID, Integer> drillingEntityHitCount = new java.util.HashMap<>();
    private static final int DRILLING_HIT_INTERVAL = 2; // hit once every 2 ticks
    private static final int DRILLING_MAX_HITS = 10; // max hits per entity
    private float creepSpeed = 0.2f;
    public boolean isHandhole = false;
    public boolean isVortex = false;
    private boolean growing = false; // Act 3 M1: +5 creep range per entity hit
    private final java.util.Set<UUID> hitEntityIds = new java.util.HashSet<>(); // for chain-hit tracking
    private LivingEntity creepTarget = null;
    private boolean isCreeping = false;
    private Vec3 creepStartPos = null;
    /** Non-null when this handhole nail is latched onto a living entity instead of a block. */
    public LivingEntity entityStuckTo = null;
    /** Per-nail maximum lifetime in ticks (default 200 = 10 sec). */
    private int maxLifetimeTicks = 200;
    /** If true, each entity hit halves the current nail speed. */
    private boolean slowsOnHit = false;
    /**
     * When true, all redirect/creep/homing logic will never target {@link StandEntity} instances
     * or entities riding a {@link NailProjectile}. Defaults to true for every nail type.
     */
    private boolean redirectIgnoresStands = true;

    public NailProjectile(Level world) {
        super(JEntityTypeRegistry.NAIL.get(), world);
        setNoGravity(true);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    public NailProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.NAIL.get(), owner, world);
        setNoGravity(true);
        setOwner(owner);
        setMaster(owner);
        setSoundEvent(SoundEvents.ARROW_HIT);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public void setPenetrating(boolean penetrating) {
        this.penetrating = penetrating;
        if (penetrating) {
            this.setPierceLevel((byte) 10);
        }
    }

    public void setDrilling(boolean drilling) {
        this.isDrilling = drilling;
        if (drilling) {
            this.setPenetrating(true);
        }
    }

    public void setCustomDamage(float damage) {
        this.customDamage = damage;
        this.setBaseDamage(damage);
    }

    /**
     * Returns true if {@code e} is a living, targetable entity for redirect/creep/homing purposes.
     * Always excludes the owner and {@link JAttackEntity} projectiles.
     * Also excludes stands and nail-riding entities when {@link #redirectIgnoresStands} is true.
     *
     * @param exclude an additional entity to exclude (e.g. the entity just hit); may be null
     */
    private boolean isValidRedirectTarget(Entity e, Entity exclude) {
        if (e == getOwner()) return false;
        if (e == exclude) return false;
        if (e instanceof JAttackEntity) return false;
        if (redirectIgnoresStands && e instanceof StandEntity<?, ?>) return false;
        if (redirectIgnoresStands && e.getVehicle() instanceof NailProjectile) return false;
        if (!(e instanceof LivingEntity le)) return false;
        return le.isAlive() && !le.isSpectator();
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        setPierceLevel((byte)(getPierceLevel()-1));
        if (level().isClientSide()) {
            return;
        }

        final Entity entity = entityHitResult.getEntity();
        final Entity owner = this.getOwner();

        if (entity == owner) return;
        if (entity instanceof JAttackEntity) return; // filter projectile attack entities
        if (entity instanceof StandEntity<?, ?>) return; // filter all stands (StandEntity extends Mob, not JAttackEntity)

        // Wormhole nails pass through everything — they're utility-only
        if (wormhole) return;

        // Handhole nails latch onto living entities (hole is placed on/above the entity)
        if (isHandhole && entity instanceof LivingEntity le) {
            entityStuckTo = le;
            noPhysics = true;
            setDeltaMovement(Vec3.ZERO);
            return; // no damage, just stick
        }

        if (!(entity instanceof LivingEntity)) return;

        LivingEntity target = JUtils.getUserIfStand((LivingEntity) entity);

        // Impact sound
        playSound(JSoundRegistry.IMPACT_11.get(), 1.0f, 1.0f);

        if (isCreeping) {
            // While homing: only stop and deal damage when we hit the actual creep target
            if (creepTarget != null && (entity == creepTarget || target == creepTarget)) {
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(JParticleTypeRegistry.HITSPARK_1.get(),
                            creepTarget.getX(), creepTarget.getY() + creepTarget.getBbHeight() * 0.5, creepTarget.getZ(),
                            1, 0, 0, 0, 0.0);
                }
                JUtils.projectileDamageLogic(this, level(), creepTarget, Vec3.ZERO, 5, 1, false,
                        customDamage, 2, CommonHitPropertyComponent.HitAnimation.LOW);
                discard();
            }
            // Pass through all other entities while homing (noclip through entities is NOT desired, but we ignore non-targets)
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(JParticleTypeRegistry.HITSPARK_1.get(),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    1, 0, 0, 0, 0.0);
        }
        JUtils.projectileDamageLogic(this, level(), target, Vec3.ZERO, 10, 1, false, customDamage, 4,
                CommonHitPropertyComponent.HitAnimation.MID, false, penetrating);

        // Fan nails: on first entity hit, halve speed and switch to homing phase through blocks
        if (fanNail && !homingFan) {
            homingFan = true;
            noPhysics = true;
            inGround = false;
            setDeltaMovement(getDeltaMovement().scale(0.15));
            return;
        }

        // Start creeping if we have creep distance and haven't started creeping yet
        if (creepDistance > 0 && !isCreeping) {
            isCreeping = true;
            creepStartPos = position();
            ticksInAir = 0;
            this.noPhysics = true;
            inGround = false;

            // Find nearby entity to creep towards (exclude current target and owner)
            List<LivingEntity> nearbyTargets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(creepDistance),
                    e -> isValidRedirectTarget(e, target));

            if (!nearbyTargets.isEmpty()) {
                // Find closest target
                LivingEntity closest = nearbyTargets.get(0);
                double closestDist = distanceToSqr(closest);
                for (LivingEntity potential : nearbyTargets) {
                    double dist = distanceToSqr(potential);
                    if (dist < closestDist) {
                        closest = potential;
                        closestDist = dist;
                    }
                }
                creepTarget = closest;
                setNoGravity(true);
                // Set reduced damage for creeping
                setCustomDamage(customDamage * 0.5f);
                // Speed halving on first hit: creep at half the current flight speed
                if (slowsOnHit) {
                    creepSpeed = (float)(getDeltaMovement().length() * 0.2f);
                }
            } else {
                discard();
            }
        } else if (!penetrating) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult blockHitResult) {
        if (level().isClientSide()) return;
        // Fan nails: hitting a block drops speed way down and switches to homing phase through blocks
        if (fanNail && !homingFan) {
            homingFan = true;
            noPhysics = true;
            inGround = false;
            setDeltaMovement(getDeltaMovement().scale(0.15));
            return;
        }
        if (creepDistance > 0 && !isCreeping) {
            BlockState hitBlock = level().getBlockState(blockHitResult.getBlockPos());
            if (!hitBlock.isSolid()) {
                super.onHitBlock(blockHitResult);
                return;
            }
            isCreeping = true;
            creepStartPos = position();
            ticksInAir = 0;
            this.noPhysics = true;
            inGround = false;
            List<LivingEntity> nearbyTargets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(creepDistance),
                    e -> isValidRedirectTarget(e, null));
            if (!nearbyTargets.isEmpty()) {
                LivingEntity closest = nearbyTargets.get(0);
                double closestDist = distanceToSqr(closest);
                for (LivingEntity potential : nearbyTargets) {
                    double dist = distanceToSqr(potential);
                    if (dist < closestDist) {
                        closest = potential;
                        closestDist = dist;
                    }
                }
                creepTarget = closest;
                setNoGravity(true);
                setCustomDamage(customDamage * 0.5f);
            } else {
                discard();
            }
        } else {
            if (level() instanceof ServerLevel serverLevel) {
                Vec3 hitPos = blockHitResult.getLocation();
                serverLevel.sendParticles(JParticleTypeRegistry.STUN_PIERCE.get(),
                        hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0.0);
            }
            super.onHitBlock(blockHitResult);
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.8F;
    }

    /** Lower the passenger's position so the camera sits near nail level, not floating above it. */
    @Override
    public double getPassengersRidingOffset() {
        return -1.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        // Block pass-through particles — fires a burst of speed particles when entering and exiting a solid block
        if (noPhysics && !inGround && level() instanceof ServerLevel serverLevel) {
            boolean currentlyInBlock = level().getBlockState(blockPosition()).isSolid();
            if (currentlyInBlock != wasInBlock) {
                serverLevel.sendParticles(JParticleTypeRegistry.SPEED_PARTICLE.get(),
                        getX(), getY(), getZ(), 5, 0.15, 0.15, 0.15, 0.05);
                if (isDrilling && noPhysics && currentlyInBlock) {
                    // Entering a block while drilling: slow down significantly
                    setDeltaMovement(getDeltaMovement().scale(0.5));
                }
                wasInBlock = currentlyInBlock;
            }
        }

        if (!inGround) {
            ++ticksInAir;
        }

        // Handhole nail stuck to a living entity — follow it
        if (entityStuckTo != null) {
            if (!entityStuckTo.isAlive()) {
                discard();
                return;
            }
            // Stay just above the entity's head
            setPos(entityStuckTo.getX(), entityStuckTo.getY() + entityStuckTo.getBbHeight() * 0.9, entityStuckTo.getZ());
            setDeltaMovement(Vec3.ZERO);
        }

        // Wormhole nails have limited lifetime (15 seconds = 300 ticks, longer than 200-tick cooldown)
        if (wormhole && ticksInAir > 300) {
            discard();
            return;
        }

        // Regular nails despawn after maxLifetimeTicks
        if (!wormhole && ticksInAir > maxLifetimeTicks) {
            discard();
            return;
        }

        // Creeping behavior
        if (isCreeping && creepTarget != null && creepTarget.isAlive()) {
            // Check if we've exceeded creep distance from start
            if (creepStartPos != null && position().distanceTo(creepStartPos) > creepDistance) {
                discard();
                return;
            }

            // Manual hit detection — noPhysics=true means onHitEntity never fires while creeping
            if (getBoundingBox().inflate(0.3).intersects(creepTarget.getBoundingBox())) {
                JUtils.projectileDamageLogic(this, level(), creepTarget, Vec3.ZERO, 5, 1, false,
                        customDamage, 2, CommonHitPropertyComponent.HitAnimation.LOW);
                playSound(JSoundRegistry.IMPACT_11.get(), 1.0f, 1.0f);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(JParticleTypeRegistry.HITSPARK_1.get(),
                            creepTarget.getX(), creepTarget.getY() + creepTarget.getBbHeight() * 0.5, creepTarget.getZ(),
                            1, 0, 0, 0, 0.0);
                }
                hitEntityIds.add(creepTarget.getUUID());
                if (slowsOnHit) { creepSpeed *= 0.2f; }

                if (growing) {
                    // Chain: +5 range, find next target excluding already-hit entities
                    creepDistance += 5.0f;
                    creepStartPos = position();
                    ticksInAir = 0;
                    List<LivingEntity> nextTargets = level().getEntitiesOfClass(LivingEntity.class,
                            getBoundingBox().inflate(creepDistance),
                            e -> isValidRedirectTarget(e, null) && !hitEntityIds.contains(e.getUUID()));
                    if (!nextTargets.isEmpty()) {
                        LivingEntity closest = nextTargets.get(0);
                        double closestDist = distanceToSqr(closest);
                        for (LivingEntity potential : nextTargets) {
                            double dist = distanceToSqr(potential);
                            if (dist < closestDist) { closest = potential; closestDist = dist; }
                        }
                        creepTarget = closest;
                        return; // Continue creeping to next target
                    }
                }
                discard();
                return;
            }

            // Move towards target through blocks
            Vec3 targetPos = creepTarget.position().add(0, creepTarget.getBbHeight() / 2, 0);
            Vec3 direction = targetPos.subtract(position()).normalize();

            setDeltaMovement(direction.scale(creepSpeed));
            setNoGravity(true);
        } else if (isCreeping) {
            // Target died or disappeared
            discard();
            return;
        }

        // Fan nail homing phase: search for nearest target every tick and fly directly toward it
        if (fanNail && homingFan && level() instanceof ServerLevel sl) {
            // Find nearest valid target within 25 blocks
            LivingEntity nearest = null;
            double nearestSq = Double.MAX_VALUE;
            for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(25.0),
                    e -> isValidRedirectTarget(e, null))) {
                double d = distanceToSqr(e);
                if (d < nearestSq) { nearest = e; nearestSq = d; }
            }

            if (nearest == null) {
                discard();
                return;
            }

            // Hit detection
            if (getBoundingBox().inflate(0.3).intersects(nearest.getBoundingBox())) {
                playSound(JSoundRegistry.IMPACT_11.get(), 1.0f, 1.0f);
                sl.sendParticles(JParticleTypeRegistry.HITSPARK_1.get(),
                        nearest.getX(), nearest.getY() + nearest.getBbHeight() * 0.5, nearest.getZ(),
                        1, 0, 0, 0, 0.0);
                JUtils.projectileDamageLogic(this, level(), nearest, Vec3.ZERO, 5, 1, false,
                        customDamage, 2, CommonHitPropertyComponent.HitAnimation.MID, false, false);
                discard();
                return;
            }

            // Fly directly toward target through blocks
            Vec3 dir = nearest.position().add(0, nearest.getBbHeight() * 0.5, 0)
                    .subtract(position()).normalize();
            setDeltaMovement(dir.scale(getDeltaMovement().length()));
            noPhysics = true;
            setNoGravity(true);
        }

        // Wormhole nail homing behavior
        if (wormhole && homing && master != null && master.isAlive() && !inGround) {
            Vec3 lookVec = master.getLookAngle();
            Vec3 currentVel = getDeltaMovement();

            // Gradually curve towards look direction
            Vec3 newVel = currentVel.lerp(lookVec.scale(currentVel.length()), 0.1);
            setDeltaMovement(newVel);
            hurtMarked = true;

            // Wormhole proximity damage: 1 HP to entities within 1.5 blocks, every 10 ticks
            if (ticksInAir % 10 == 0 && level() instanceof ServerLevel sl) {
                AABB wormholeAoe = getBoundingBox().inflate(1.5);
                level().getEntitiesOfClass(LivingEntity.class, wormholeAoe,
                        e -> e != master && e.isAlive() && !e.isSpectator()
                                && !(e instanceof JAttackEntity) && !(e instanceof StandEntity))
                        .forEach(e -> e.hurt(sl.damageSources().arrow(this, master), 1.0f));
            }
        }

        // Drilling behavior - hit entities once every DRILLING_HIT_INTERVAL ticks per entity
        if (isDrilling && !inGround) {
            Level world = level();
            if (!world.isClientSide()) {
                int currentTick = tickCount;
                AABB hitBox = getBoundingBox().inflate(0.5);
                List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, hitBox,
                        entity -> entity != getOwner() && !entity.isSpectator() && entity.isAlive()
                                && !(entity.getVehicle() instanceof NailProjectile));

                for (LivingEntity target : targets) {
                    java.util.UUID uid = target.getUUID();
                    int hitCount = drillingEntityHitCount.getOrDefault(uid, 0);
                    if (hitCount >= DRILLING_MAX_HITS) continue;
                    int lastHit = drillingEntityLastHit.getOrDefault(uid, -DRILLING_HIT_INTERVAL);
                    if (currentTick - lastHit >= DRILLING_HIT_INTERVAL) {
                        LivingEntity actualTarget = JUtils.getUserIfStand(target);
                        if (actualTarget == null) continue;

                        JUtils.projectileDamageLogic(this, world, actualTarget, Vec3.ZERO, 5, 1, false,
                                customDamage, 2, CommonHitPropertyComponent.HitAnimation.LOW, false, true);

                        // Slow DOWN dramatically when drilling through an entity
                        setDeltaMovement(getDeltaMovement().scale(0.25));

                        playSound(SoundEvents.ARROW_HIT, 0.5f, 1.2f);
                        if (world instanceof ServerLevel sl) {
                            sl.sendParticles(JParticleTypeRegistry.HITSPARK_1.get(),
                                    actualTarget.getX(), actualTarget.getY() + actualTarget.getBbHeight() * 0.5, actualTarget.getZ(),
                                    1, 0, 0, 0, 0.0);
                        }
                        drillingEntityLastHit.put(uid, currentTick);
                        drillingEntityHitCount.put(uid, hitCount + 1);
                    }
                }
            }
        }

        // Vortex behavior - pull entities toward the nail
        if (isVortex && !inGround) {
            Level world = level();
            if (!world.isClientSide()) {
                AABB pullBox = getBoundingBox().inflate(5.0);
                List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, pullBox,
                        entity -> entity.isAlive() && !entity.isSpectator());
                Vec3 center = position();
                for (LivingEntity target : targets) {
                    Vec3 pull = center.subtract(target.position()).normalize().scale(0.5f);
                    target.push(pull.x, pull.y, pull.z);
                    target.hurtMarked = true;
                    // Deal 4 damage once per second
                    if (ticksInAir % 20 == 0 && world instanceof ServerLevel sl) {
                        DamageSource dmgSrc = master != null
                                ? sl.damageSources().arrow(this, master)
                                : sl.damageSources().generic();
                        target.hurt(dmgSrc, 4.0f);
                    }
                }
            }
        }

        // Range limit
        if (ticksInAir * getDeltaMovement().length() > maxRange) {
            discard();
            return;
        }

        // Particle trail
        if (this.level() instanceof ServerLevel serverLevel && !this.inGround) {
            Vec3 velocity = this.getDeltaMovement();
            double speed = velocity.length();

            // Wormhole nails always emit a guaranteed pulse at exact position — no spread, no gaps
            if (wormhole) {
                serverLevel.sendParticles(JParticleTypeRegistry.NAIL_TRAIL.get(),
                        getX(), getY(), getZ(), 2, 0, 0, 0, 0.0);
            }

            // Spawn trail particles proportional to distance travelled this tick
            if (speed > 0.05) {
                double currentX = this.getX();
                double currentY = this.getY();
                double currentZ = this.getZ();

                double prevX = currentX - velocity.x;
                double prevY = currentY - velocity.y;
                double prevZ = currentZ - velocity.z;

                double distance = Math.sqrt(
                        Math.pow(currentX - prevX, 2) +
                                Math.pow(currentY - prevY, 2) +
                                Math.pow(currentZ - prevZ, 2)
                );

                int particleCount = spinning ? Math.max(2, (int)(distance * 6)) : Math.max(1, (int)(distance * 3));

                for (int i = 0; i < particleCount; i++) {
                    double t = (double) i / particleCount;
                    double x = prevX + (currentX - prevX) * t;
                    double y = prevY + (currentY - prevY) * t;
                    double z = prevZ + (currentZ - prevZ) * t;

                    serverLevel.sendParticles(JParticleTypeRegistry.NAIL_TRAIL.get(),
                            x, y, z, 1, 0, 0, 0, 0.0);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.ticksInAir);
        tag.putBoolean("Spinning", this.spinning);
        tag.putBoolean("Wormhole", this.wormhole);
        tag.putInt("ChargeTime", this.chargeTime);
        tag.putBoolean("Penetrating", this.penetrating);
        tag.putBoolean("Drilling", this.isDrilling);
        tag.putBoolean("FanNail", this.fanNail);
        tag.putBoolean("HomingFan", this.homingFan);
        tag.putFloat("CustomDamage", this.customDamage);
        tag.putFloat("MaxRange", this.maxRange);
        tag.putFloat("CreepDistance", this.creepDistance);
        tag.putBoolean("IsCreeping", this.isCreeping);
        tag.putInt("LastHitTick", this.lastHitTick);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksInAir = tag.getInt("Life");
        this.spinning = tag.getBoolean("Spinning");
        this.wormhole = tag.getBoolean("Wormhole");
        this.chargeTime = tag.getInt("ChargeTime");
        this.penetrating = tag.getBoolean("Penetrating");
        this.isDrilling = tag.getBoolean("Drilling");
        this.fanNail = tag.getBoolean("FanNail");
        this.homingFan = tag.getBoolean("HomingFan");
        this.customDamage = tag.getFloat("CustomDamage");
        this.maxRange = tag.getFloat("MaxRange");
        this.creepDistance = tag.getFloat("CreepDistance");
        this.isCreeping = tag.getBoolean("IsCreeping");
        this.lastHitTick = tag.getInt("LastHitTick");
    }

    // ===== FACTORY METHODS =====

    /**
     * Tusk Act 1 - Basic nail (10 blocks)
     */
    public static @Nullable NailProjectile fromTuskAct1(TuskAct1Entity tusk, float maxRange) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setCustomDamage(4.0f);
        nail.maxRange = maxRange;
        return nail;
    }

    /**
     * Tusk Act 1 - Toenail shot (free, does not consume nails or hunger)
     */
    public static @Nullable NailProjectile fromTuskAct1Toenail(TuskAct1Entity tusk, float maxRange) {
        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setCustomDamage(5.0f);
        nail.maxRange = maxRange;
        return nail;
    }

    /**
     * Tusk Act 2 - Free toenail (crouching variant, no nail cost, same as Act 1 toenail)
     */
    public static @Nullable NailProjectile fromTuskAct1Toenail2(TuskAct2Entity tusk, float maxRange) {
        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setCustomDamage(5.0f);
        nail.maxRange = maxRange;
        return nail;
    }

    /**
     * Tusk Act 2 - Golden Rectangle Nail (15 blocks, creeping)
     */
    public static @Nullable NailProjectile fromTuskAct2(TuskAct2Entity tusk, float maxRange, float creepDistance) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setCustomDamage(6.0f); // Act 2: 1.5x Act 1
        nail.maxRange = maxRange;
        nail.creepDistance = creepDistance;
        nail.maxLifetimeTicks = 400; // 15 seconds
        nail.slowsOnHit = true;
        return nail;
    }

    /**
     * Tusk Act 2 - Drill Shot (heavy, holdable)
     * Damage scales with charge: 1.0 HP (0.5 hearts) to 5.0 HP (2.5 hearts).
     * At 2+ HP damage (above ~25% charge), drills through blocks (noPhysics).
     * Slows dramatically on each entity hit and on block entry.
     * Speed scales with charge: 1.0x–2.0x base speed.
     */
    public static @Nullable NailProjectile fromTuskAct2Perfect(TuskAct2Entity tusk, float nailCost, int chargeTime, float maxRange) {
        if (!tusk.drainNails(nailCost)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setChargeTime(chargeTime);
        nail.maxRange = 20.0f;
        nail.setDrilling(true);

        // Damage: 1.0 HP (0.5 hearts) at no charge → 5.0 HP (2.5 hearts) at full charge
        float damage = 1.0f + (Math.min(chargeTime, 100) / 100.0f) * 4.0f;
        nail.setCustomDamage(damage);

        // At 2+ HP damage, nail drills through blocks
        nail.noPhysics = damage >= 2.0f;

        return nail;
    }

    /**
     * Tusk Act 2 - Fan Nail (1 of 5 in a spread). Homes toward nearest entity.
     */
    public static @Nullable NailProjectile fromTuskAct2Fan(TuskAct2Entity tusk) {
        if (!tusk.drainNails(1.0f)) return null; // 1 nail per projectile, 5 total
        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setCustomDamage(4.0f);
        nail.maxRange = 20.0f;
        nail.fanNail = true;
        return nail;
    }

    /**
     * Tusk Act 3 - Regular nail (same stats as Act 2 golden rectangle)
     */
    public static @Nullable NailProjectile fromTuskAct3(TuskAct3Entity tusk, float maxRange, float creepDistance) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setCustomDamage(8.0f); // Act 3: 2x Act 1
        nail.maxRange = maxRange;
        nail.creepDistance = creepDistance;
        nail.growing = true; // +5 creep range per chain hit
        nail.slowsOnHit = true; // maxLifetimeTicks stays 200 (10 sec)
        return nail;
    }

    /**
     * Tusk Act 3 - Wormhole nail
     */
    public static @Nullable NailProjectile wormholeFromTuskAct3(TuskAct3Entity tusk) {
        if (!tusk.drainNails(NAIL_COST)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.wormhole = true;
        nail.homing = true;
        nail.noPhysics = true; // noclip through blocks
        nail.setCustomDamage(0.0f);
        return nail;
    }

    /**
     * Tusk Act 3 - Handhole nail (sticks in ground, redirects shots)
     */
    public static @Nullable NailProjectile handholeFromTuskAct3(TuskAct3Entity tusk) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.isHandhole = true;
        nail.setCustomDamage(0.0f);
        nail.maxRange = 20.0f;
        return nail;
    }

    /**
     * Tusk Act 3 - Handhole shot (fired FROM the handhole position)
     */
    public static @Nullable NailProjectile handholeShot(TuskAct3Entity tusk) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setCustomDamage(7.0f);
        nail.maxRange = 15.0f;
        nail.creepDistance = 8.0f;
        return nail;
    }

    /**
     * Tusk Act 3 - Vortex nail (moves, pulls entities)
     */
    public static @Nullable NailProjectile vortexFromTuskAct3(TuskAct3Entity tusk) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.isVortex = true;
        nail.setCustomDamage(3.0f);
        nail.maxRange = 25.0f;
        return nail;
    }

    // Getters
    public boolean isWormhole() {
        return wormhole;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public int getChargeTime() {
        return chargeTime;
    }

    public boolean isDrilling() {
        return isDrilling;
    }
}