package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.*;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.arna.jcraft.common.util.JUtils.RAD_TO_DEG;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart head;
    @Shadow
    @Final
    public ModelPart hat;
    @Shadow
    @Final
    public ModelPart body;
    @Shadow
    @Final
    public ModelPart rightArm;
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightLeg;
    @Shadow
    @Final
    public ModelPart leftLeg;
    @Shadow
    public
    BipedEntityModel.ArmPose leftArmPose;
    @Shadow
    public
    BipedEntityModel.ArmPose rightArmPose;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V", shift = At.Shift.BEFORE))
    public void jcraft$setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        HitPropertyComponent hitProperties = JComponents.getHitProperties(livingEntity);
        long endHitAnimTime = hitProperties.endHitAnimTime();
        if (endHitAnimTime > 0) {
            JClientUtils.animateHit(hitProperties.getHitAnimation(), livingEntity.isAlive() ? endHitAnimTime : 0, hitProperties.getRandomRotation(), head, hat, body, rightArm, leftArm, rightLeg, leftLeg);
            return;
        }

        if (livingEntity.isHolding(JObjectRegistry.FV_REVOLVER))
            CrossbowPosing.hold(rightArm, leftArm, head, livingEntity.getMainArm() == Arm.RIGHT);

        if (livingEntity.getPose() == EntityPose.STANDING) {
            JClientUtils.resetPartAngles(body);

            if (livingEntity.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
                switch (stand.getStandType()) {
                    case STAR_PLATINUM -> {
                        // RAD_TO_DEG = pi/180
                        if (leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                            leftArm.pitch = 0;
                            leftArm.yaw = -15 * RAD_TO_DEG;
                            leftArm.roll = 5 * RAD_TO_DEG;
                        }

                        if (rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                            rightArm.roll = 15 * RAD_TO_DEG;
                            rightArm.pitch *= 0.5F;
                        }
                    }

                    case THE_WORLD -> {
                        // Arms near hips, the DIO pose in HFTF
                        if (leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                            leftArm.yaw = 15 * RAD_TO_DEG;
                            leftArm.roll = 2 * RAD_TO_DEG;
                        }

                        if (rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                            rightArm.yaw = -15 * RAD_TO_DEG;
                            rightArm.roll = -2 * RAD_TO_DEG;
                        }

                        if (!livingEntity.isSprinting()) {
                            leftArm.pitch -= 10F * RAD_TO_DEG;
                            rightArm.pitch -= 10F * RAD_TO_DEG;
                            body.pitch -= 10F * RAD_TO_DEG;

                            leftLeg.pivotZ -= 2F;
                            rightLeg.pivotZ -= 2F;

                            leftArm.pivotZ += 0.25F;
                            rightArm.pivotZ += 0.25F;
                            leftArm.pivotX += 0.5F;
                            rightArm.pivotX -= 0.5F;
                        }
                    }

                    case KING_CRIMSON -> { // Back towards KC
                        if (JUtils.deltaPos(livingEntity).horizontalLengthSquared() <= 0) {
                            body.yaw += 30 * RAD_TO_DEG;

                            if (leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                                leftArm.yaw += 30 * RAD_TO_DEG;
                                leftArm.pivotZ -= 2.1F;
                            }

                            if (rightArmPose == BipedEntityModel.ArmPose.EMPTY || rightArmPose == BipedEntityModel.ArmPose.ITEM) {
                                rightArm.yaw += 30 * RAD_TO_DEG;
                                rightArm.pivotZ += 2.1F;
                            }

                            leftLeg.pivotZ -= 1F;
                            rightLeg.pivotZ += 1.5F;

                            rightLeg.yaw += 45 * RAD_TO_DEG;
                        }
                    }

                    case KILLER_QUEEN, KILLER_QUEEN_BITES_THE_DUST -> {
                        if (JUtils.deltaPos(livingEntity).horizontalLengthSquared() <= 0) {
                            if (leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                                leftArm.yaw += 15 * RAD_TO_DEG;
                                leftArm.pitch -= 15 * RAD_TO_DEG;
                                leftArm.roll += 45 * RAD_TO_DEG;
                            }

                            if (rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                                rightArm.yaw -= 15 * RAD_TO_DEG;
                                rightArm.pitch -= 15 * RAD_TO_DEG;
                                rightArm.roll -= 45 * RAD_TO_DEG;
                            }
                        }

                        body.pitch -= 5F * RAD_TO_DEG;
                        leftLeg.pivotZ -= 1F;
                        rightLeg.pivotZ -= 1F;
                    }

                    case THE_WORLD_OVER_HEAVEN, GOLD_EXPERIENCE_REQUIEM -> {
                        // Floating
                        float heightOffset = 1.0f + MathHelper.sin(h / 10);
                        head.pivotY -= heightOffset;
                        body.pivotY -= heightOffset;

                        leftArm.pivotY -= heightOffset;
                        rightArm.pivotY -= heightOffset;

                        // Leaning while moving
                        float speedInfluence = (float) JUtils.deltaPos(livingEntity).horizontalLength() * 45f * RAD_TO_DEG;

                        body.pitch += speedInfluence;

                        leftLeg.pivotY -= heightOffset + 1f + MathHelper.sin(speedInfluence) * 6f;
                        leftLeg.pivotZ += MathHelper.sin(speedInfluence) * 12f - 2F;
                        rightLeg.pivotY -= heightOffset + MathHelper.sin(speedInfluence) * 6f;
                        rightLeg.pivotZ += MathHelper.sin(speedInfluence) * 12f;

                        rightLeg.pitch = speedInfluence;
                        leftLeg.pitch = 15 * RAD_TO_DEG + speedInfluence;

                        leftArm.pitch *= 0.25f;
                        rightArm.pitch *= 0.25f;

                        // One arm stretched out
                        if (leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                            leftArm.roll = -45 * RAD_TO_DEG + MathHelper.sin(h / 10) / 8f;
                            leftArm.pitch = speedInfluence;
                        }

                        if (rightArmPose == BipedEntityModel.ArmPose.EMPTY)
                            rightArm.pitch = speedInfluence;
                    }
                }
            }
        }
    }
}
