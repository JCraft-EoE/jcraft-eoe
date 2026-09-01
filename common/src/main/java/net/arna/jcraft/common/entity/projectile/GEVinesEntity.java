package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class GEVinesEntity extends AbstractArrow {
    private final Vec3 launchVec;
    private final LivingEntity livingOwner;
    int hitCount = 0;

    public GEVinesEntity(Level world) {
        this(world, null, Vec3.ZERO);
    }

    public GEVinesEntity(Level world, LivingEntity owner, Vec3 launchVec) {
        super(JEntityTypeRegistry.GE_VINES.get(), world);
        this.setOwner(owner);
        this.setInvulnerable(true);
        this.setSilent(true);
        this.livingOwner = owner;
        this.pickup = Pickup.DISALLOWED;
        this.launchVec = launchVec;
    }

    private boolean lockRotation = false;

    @Override
    public void setXRot(float xRot) {
        if (lockRotation) return;
        super.setXRot(xRot);
    }
    @Override
    public void setYRot(float yRot) {
        if (lockRotation) return;
        super.setYRot(yRot);
    }

    @Override
    public void tick() {
        lockRotation = true;
        super.tick();
        lockRotation = false;

        if (tickCount > 72) {
            discard();
        }

        if (level().isClientSide || livingOwner == null) {
            return;
        }

        if (tickCount == 16 || tickCount == 36 || tickCount == 42 || tickCount == 49) {
            final DamageSource ds = level().damageSources().mobAttack(livingOwner);
            final var kbVec = launchVec.scale( hitCount * 0.15 - 1.0);
            final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), position().add(launchVec.normalize()), 2.5 - hitCount * 0.25, Set.of(this));

            boolean anyHit = false;

            for (LivingEntity living : hurt) {
                final LivingEntity target = JUtils.getUserIfStand(living);

                if (!JUtils.canDamage(ds, target))
                    continue;

                if (livingOwner == target)
                    continue;

                damageLogic(level(), target, kbVec, 22 - hitCount * 2, StunType.BURSTABLE.ordinal(),
                        false, 5f, true, 11, ds, livingOwner, CommonHitPropertyComponent.HitAnimation.MID, false);

                anyHit = true;
            }

            if (anyHit)
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, getSoundSource(), 1, 1);

            hitCount++;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // erased
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean startRiding(@NonNull Entity entity, boolean force) {
        return false;
    }

    public static final AzCommand ANIMATION = AzCommand.controllerBuilder().
            playSequence(
                    JCraft.BASE_CONTROLLER,
                    sequenceBuilder -> sequenceBuilder.queue(
                            "animation.gevine.attack",
                            props -> props.withPlayBehavior(AzPlayBehaviors.HOLD_ON_LAST_FRAME)
                    )
            )
            .setFreezeTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setStartTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setSpeed(JCraft.BASE_CONTROLLER, 1)
            .setRepeatAmount(JCraft.BASE_CONTROLLER, 0)
            .setReverseAnimation(JCraft.BASE_CONTROLLER, false)
            .build();
}
