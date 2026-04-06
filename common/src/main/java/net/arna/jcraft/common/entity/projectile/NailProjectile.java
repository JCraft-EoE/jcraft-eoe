package net.arna.jcraft.common.entity.projectile;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
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
import net.minecraft.world.phys.AABB;
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
    private final Map<UUID, Integer> drillingEntityLastHit = new java.util.HashMap<>();
    private static final int DRILLING_HIT_INTERVAL = 4; // 0.2 seconds between hits per entity
    private float creepSpeed = 0.5f;
    public boolean isHandhole = false;
    public boolean isVortex = false;
    private LivingEntity creepTarget = null;
    private boolean isCreeping = false;
    private Vec3 creepStartPos = null;

    public NailProjectile(Level world) {
        super(JEntityTypeRegistry.NAIL.get(), world);
        setNoGravity(true);
    }

    public NailProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.NAIL.get(), owner, world);
        setNoGravity(true);
        setOwner(owner);
        setMaster(owner);
        setSoundEvent(SoundEvents.ARROW_HIT);
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

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        setPierceLevel((byte)(getPierceLevel()-1));
        if (level().isClientSide()) {
            return;
        }

        final Entity entity = entityHitResult.getEntity();
        final Entity owner = this.getOwner();

        if (owner != null && owner.hasPassenger(entity) || entity == owner) return;
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == owner) return;

        // Wormhole nails don't deal damage
        if (wormhole) {
            inGround = true;
            return;
        }

        if (!(entity instanceof LivingEntity)) return;

        LivingEntity target = JUtils.getUserIfStand((LivingEntity) entity);

        // Handle damage
        if (isCreeping) {
            JUtils.projectileDamageLogic(this, level(), creepTarget, Vec3.ZERO, 5, 1, false,
                    customDamage, 2, CommonHitPropertyComponent.HitAnimation.LOW);
        }
        else {
            JUtils.projectileDamageLogic(this, level(), target, Vec3.ZERO, 10, 1, false, customDamage, 4,
                    CommonHitPropertyComponent.HitAnimation.MID, false, penetrating);
        }
        playSound(SoundEvents.ARROW_HIT, 1, 1);

        // Start creeping if we have creep distance and haven't started creeping yet
        if (creepDistance > 0 && !isCreeping) {
            inGround = true;
            isCreeping = true;
            creepStartPos = position();
            ticksInAir = 0;

            // Find nearby entity to creep towards (exclude current target and owner)
            List<LivingEntity> nearbyTargets = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(creepDistance),
                    e -> e != owner && e != target && !e.isSpectator() && e.isAlive());

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
            } else {
                discard();
            }
        } else if (!penetrating) {
            discard();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.8F;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        if (!inGround) {
            ++ticksInAir;
        }

        // Wormhole nails have limited lifetime (5 seconds = 100 ticks)
        if (wormhole && ticksInAir > 100) {
            discard();
            return;
        }

        // Regular nails despawn after 10 seconds
        if (ticksInAir > 200) {
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

            // Move towards target through blocks
            Vec3 targetPos = creepTarget.position().add(0, creepTarget.getBbHeight() / 2, 0);
            Vec3 currentPos = position();
            Vec3 direction = targetPos.subtract(currentPos).normalize();

            setDeltaMovement(direction.scale(creepSpeed));
            setNoGravity(true);
            // handle damage logic in onHitEntity()
        } else if (isCreeping) {
            // Target died or disappeared
            discard();
            return;
        }

        // Wormhole nail homing behavior
        if (wormhole && homing && master != null && master.isAlive() && !inGround) {
            Vec3 lookVec = master.getLookAngle();
            Vec3 currentVel = getDeltaMovement();

            // Gradually curve towards look direction
            Vec3 newVel = currentVel.lerp(lookVec.scale(currentVel.length()), 0.1);
            setDeltaMovement(newVel);
            hurtMarked = true;
        }

        // Drilling behavior - hit entities once every DRILLING_HIT_INTERVAL ticks per entity
        if (isDrilling && !inGround) {
            Level world = level();
            if (!world.isClientSide()) {
                int currentTick = tickCount;
                AABB hitBox = getBoundingBox().inflate(0.5);
                List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, hitBox,
                        entity -> entity != getOwner() && !entity.isSpectator() && entity.isAlive());

                for (LivingEntity target : targets) {
                    java.util.UUID uid = target.getUUID();
                    int lastHit = drillingEntityLastHit.getOrDefault(uid, -DRILLING_HIT_INTERVAL);
                    if (currentTick - lastHit >= DRILLING_HIT_INTERVAL) {
                        LivingEntity actualTarget = JUtils.getUserIfStand(target);
                        JUtils.projectileDamageLogic(this, world, actualTarget, Vec3.ZERO, 5, 1, false,
                                customDamage, 2, CommonHitPropertyComponent.HitAnimation.LOW, false, true);
                        playSound(SoundEvents.ARROW_HIT, 0.5f, 1.2f);
                        drillingEntityLastHit.put(uid, currentTick);
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
                    Vec3 pull = center.subtract(target.position()).normalize().scale(0.3);
                    target.push(pull.x, pull.y, pull.z);
                    target.hurtMarked = true;
                }
            }
        }

        // Range limit
        if (ticksInAir * getDeltaMovement().length() > maxRange) {
            discard();
            return;
        }

        // Blue particle trail
        if (this.level() instanceof ServerLevel serverLevel && !this.inGround) {
            Vec3 velocity = this.getDeltaMovement();
            double speed = velocity.length();

            // Only spawn particles if nail is moving
            if (speed > 0.1) {
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

                // Denser trail for spinning nails
                int particleCount = spinning ? Math.max(2, (int)(distance * 6)) : Math.max(1, (int)(distance * 3));

                for (int i = 0; i < particleCount; i++) {
                    double t = (double) i / particleCount;
                    double x = prevX + (currentX - prevX) * t;
                    double y = prevY + (currentY - prevY) * t;
                    double z = prevZ + (currentZ - prevZ) * t;

                    serverLevel.sendParticles(
                            JParticleTypeRegistry.NAIL_TRAIL.get(),
                            x, y, z,
                            1,
                            0, 0, 0,
                            0.0
                    );
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
     * Tusk Act 1 - Toenail shot (5 blocks, 0.5 nail cost)
     */
    public static @Nullable NailProjectile fromTuskAct1Toenail(TuskAct1Entity tusk, float maxRange) {
        if (!tusk.drainNails(0.5f)) return null;

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
        nail.setCustomDamage(7.0f);
        nail.maxRange = maxRange;
        nail.creepDistance = creepDistance;
        return nail;
    }

    /**
     * Tusk Act 2 - Perfect Golden Rotation (20 blocks, drilling once per tick)
     * Damage scales with charge: 6-12
     * Speed scales with charge: 1.0x-2.0x
     */
    public static @Nullable NailProjectile fromTuskAct2Perfect(TuskAct2Entity tusk, float nailCost, int chargeTime, float maxRange) {
        if (!tusk.drainNails(nailCost)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setChargeTime(chargeTime);
        nail.maxRange = maxRange;
        nail.setDrilling(true); // Enable drilling mode - hits once per tick

        // Damage: 6-12 based on charge (0-100 ticks)
        float damage = 6.0f + (chargeTime / 100.0f * 6.0f);
        nail.setCustomDamage(damage);

        return nail;
    }

    /**
     * Tusk Act 3 - Regular nail (same stats as Act 2 golden rectangle)
     */
    public static @Nullable NailProjectile fromTuskAct3(TuskAct3Entity tusk, float maxRange, float creepDistance) {
        if (!tusk.drainNails(1.0f)) return null;

        NailProjectile nail = new NailProjectile(tusk.level(), tusk.getUserOrThrow());
        nail.setSpinning(true);
        nail.setCustomDamage(7.0f);
        nail.maxRange = maxRange;
        nail.creepDistance = creepDistance;
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