package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class SunBeamProjectile extends AbstractArrow {
    private static final int MAX_LENGTH = 64;

    private int length = 0;
    private final @Nullable TheSunEntity sun;
    private DamageSource damageSource;

    public static final EntityDataAccessor<Integer> SKIN;

    static {
        SKIN = SynchedEntityData.defineId(SunBeamProjectile.class, EntityDataSerializers.INT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
    }

    public int getSkin() {
        return entityData.get(SKIN);
    }

    public void setSkin(int skin) {
        entityData.set(SKIN, skin);
    }

    public SunBeamProjectile(Level world, @Nullable LivingEntity owner, @Nullable TheSunEntity sun) {
        super(JEntityTypeRegistry.SUN_BEAM.get(), world);
        setOwner(owner);
        setNoGravity(true);
        this.sun = sun;
        noCulling = true;
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        damageSource = JDamageSources.create(level(), DamageTypes.MOB_ATTACK, owner);
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    // Light isn't very heavy
    @Override
    public void push(@NonNull Entity entity) {
    }

    @Override
    public boolean canCollideWith(@NonNull Entity other) {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    private boolean allowRotation = true;
    @Override
    public void setXRot(float xRot) {
        if (allowRotation) super.setXRot(xRot);
    }

    @Override
    public void setYRot(float yRot) {
        if (allowRotation) super.setYRot(yRot);
    }

    @Override
    public void tick() {
        allowRotation = false;
        super.tick();

        if (sun != null) setPos(position().add(JUtils.deltaPos(sun)));
        final Vec3 curPos = position();

        if (tickCount > 5 && tickCount <= 10) {
            length += MAX_LENGTH / 5;
        }

        if (level().isClientSide()) {
            FIRE.sendForEntity(this);

            if (tickCount <= 20) {
                Vec3 velocity = getDeltaMovement().scale(random.nextDouble() * length * 10.0);
                level().addParticle(
                        getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        curPos.x + random.nextGaussian() * 0.25,
                        curPos.y + random.nextGaussian() * 0.25,
                        curPos.z + random.nextGaussian() * 0.25,
                        velocity.x,
                        velocity.y,
                        velocity.z
                );
            }
        } else {
            if (tickCount <= 20) {
                if (tickCount % 3 == 0 && getOwner() instanceof LivingEntity owner) {
                    Set<Entity> filter = new HashSet<>();
                    filter.add(owner);
                    filter.add(sun);
                    filter.add(this);
                    if (owner.isVehicle()) {
                        filter.addAll(owner.getPassengers());
                    }

                    // Hitbox check between current and previous position
                    final Vec3 towardsVec = getDeltaMovement().normalize();
                    final List<LivingEntity> hurtAll = new ArrayList<>();
                    double hitboxSize = 2.0;
                    for (double i = 0.0; i < length / hitboxSize; i++) {
                        Vec3 laserPos = curPos.add(towardsVec.scale(i * hitboxSize));
                        Set<LivingEntity> targets = JUtils.generateHitbox(level(), laserPos, hitboxSize, filter);
                        targets.removeIf(hurtAll::contains);
                        hurtAll.addAll(targets);
                        TheSunEntity.dryOut((ServerLevel) level(), BlockPos.containing(laserPos));
                    }
                    hurtAll.removeIf(e -> !canDamage(damageSource, e));

                    if (!hurtAll.isEmpty()) {
                        for (LivingEntity l : hurtAll) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            int stun = 10;
                            damageLogic(level(), target, Vec3.ZERO, stun, 1, false, 1f,
                                    true, 2, damageSource, owner, CommonHitPropertyComponent.HitAnimation.values()[random.nextInt(3)]);
                        }

                        Vec3 hitPos = hurtAll.get(0).position();
                        JCraft.createParticle((ServerLevel) level(),
                                hitPos.x + random.nextGaussian() * 0.25,
                                hitPos.y + random.nextGaussian() * 0.25,
                                hitPos.z + random.nextGaussian() * 0.25,
                                JParticleType.HIT_SPARK_1);
                    }
                }
            } else if (tickCount >= 24) {
                kill();
            }
        }
    }

    private static final AzCommand FIRE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.sunbeam.fire");
}
