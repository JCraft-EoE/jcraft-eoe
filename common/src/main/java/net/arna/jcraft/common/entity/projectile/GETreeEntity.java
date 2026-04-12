package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class GETreeEntity extends AbstractArrow {
    private final Vec3 launchVec;
    private final LivingEntity livingOwner;

    public GETreeEntity(Level world) {
        this(world, null, Vec3.ZERO);
    }

    public GETreeEntity(Level world, LivingEntity owner, Vec3 launchVec) {
        super(JEntityTypeRegistry.GE_TREE.get(), world);
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
        if (tickCount > 120) {
            discard();
        }

        if (level().isClientSide || livingOwner == null) {
            return;
        }

        if (tickCount == 4) {
            final DamageSource ds = level().damageSources().mobAttack(livingOwner);
            final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), position().add(launchVec.normalize()), 2.5, Set.of(this, livingOwner));

            for (LivingEntity living : hurt) {
                if (!JUtils.canDamage(ds, living)) {
                    continue;
                }

                final LivingEntity target = JUtils.getUserIfStand(living);
                if (livingOwner != target) {
                    damageLogic(level(), target, Vec3.ZERO, 25, 3,
                            false, 7f, false, 11, ds, livingOwner, CommonHitPropertyComponent.HitAnimation.MID, false);
                }
                JUtils.addVelocity(target, launchVec.x, launchVec.y, launchVec.z);
            }
        }
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
                        "animation.getree.spawn",
                    props -> props.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE)
                ).queue(
                        "animation.getree.idle",
                        props -> props.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE)
                ).queue(
                        "animation.getree.return",
                        props -> props.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE)
                )
            )
            .setFreezeTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setStartTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setSpeed(JCraft.BASE_CONTROLLER, 1)
            .setRepeatAmount(JCraft.BASE_CONTROLLER, 0)
            .setReverseAnimation(JCraft.BASE_CONTROLLER, false)
            .build();
}
