package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.entity.*;
import net.arna.jcraft.common.util.JCraftUtils;
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

    private void setScale(ModelPart p, float f) {
        p.xScale = f;
        p.yScale = f;
        p.zScale = f;
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyTransform(Lnet/minecraft/client/model/ModelPart;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void jcraft$setAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo info) {
        if (!JCraftUtils.shouldRender(livingEntity)) {
            setScale(this.head, 0f);
            setScale(this.hat, 0f);
            setScale(this.body, 0f);
            setScale(this.rightArm, 0f);
            setScale(this.leftArm, 0f);
            setScale(this.rightLeg, 0f);
            setScale(this.leftLeg, 0f);
            info.cancel();
        } else {
            setScale(this.head, 1f);
            setScale(this.hat, 1f);
            setScale(this.body, 1f);
            setScale(this.rightArm, 1f);
            setScale(this.leftArm, 1f);
            setScale(this.rightLeg, 1f);
            setScale(this.leftLeg, 1f);
        }

        if (livingEntity.isHolding(JObjectRegistry.FVREVOLVER))
            CrossbowPosing.hold(this.rightArm, this.leftArm, this.head, livingEntity.getMainArm() == Arm.RIGHT);

        if (livingEntity.getPose() == EntityPose.STANDING && livingEntity.getFirstPassenger() instanceof StandEntity stand) {
            //im sorry but you cant make a switch statement for instanceof
            if (stand instanceof StarPlatinumEntity) {
                // 0.017453292F = pi/180
                if (this.leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                    this.leftArm.pitch = 0;
                    this.leftArm.yaw = -15 * 0.017453292F;
                    this.leftArm.roll = 5 * 0.017453292F;
                }

                if (this.rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                    this.rightArm.roll = 15 * 0.017453292F;
                    this.rightArm.pitch *= 0.5F;
                }
            }

            if (stand instanceof TheWorldEntity) {
                // Arms near hips, the DIO pose in HFTF
                if (this.leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                    this.leftArm.yaw = 15 * 0.017453292F;
                    this.leftArm.roll = 2 * 0.017453292F;
                }

                if (this.rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                    this.rightArm.yaw = -15 * 0.017453292F;
                    this.rightArm.roll = -2 * 0.017453292F;
                }

                if (!livingEntity.isSprinting()) {
                    this.leftArm.pitch -= 10F * 0.017453292F;
                    this.rightArm.pitch -= 10F * 0.017453292F;
                    this.body.pitch -= 10F * 0.017453292F;

                    this.leftLeg.pivotZ -= 2F;
                    this.rightLeg.pivotZ -= 2F;

                    this.leftArm.pivotZ += 0.25F;
                    this.rightArm.pivotZ += 0.25F;
                    this.leftArm.pivotX += 0.5F;
                    this.rightArm.pivotX -= 0.5F;
                }
            }

            if (stand instanceof KingCrimsonEntity) { // Back towards KC
                if (JCraftUtils.deltaPos(livingEntity).horizontalLengthSquared() <= 0) {
                    this.body.yaw += 30 * 0.017453292F;

                    if (this.leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                        this.leftArm.yaw += 30 * 0.017453292F;
                        this.leftArm.pivotZ -= 2.1F;
                    }

                    if (this.rightArmPose == BipedEntityModel.ArmPose.EMPTY || this.rightArmPose == BipedEntityModel.ArmPose.ITEM) {
                        this.rightArm.yaw += 30 * 0.017453292F;
                        this.rightArm.pivotZ += 2.1F;
                    }

                    this.leftLeg.pivotZ -= 1F;
                    this.rightLeg.pivotZ += 1.5F;

                    this.rightLeg.yaw += 45 * 0.017453292F;
                }
            }

            if (stand instanceof KillerQueenEntity) {
                if (JCraftUtils.deltaPos(livingEntity).horizontalLengthSquared() <= 0) {
                    if (this.leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                        this.leftArm.yaw += 15 * 0.017453292F;
                        this.leftArm.pitch -= 15 * 0.017453292F;
                        this.leftArm.roll += 45 * 0.017453292F;
                    }

                    if (this.rightArmPose == BipedEntityModel.ArmPose.EMPTY) {
                        this.rightArm.yaw -= 15 * 0.017453292F;
                        this.rightArm.pitch -= 15 * 0.017453292F;
                        this.rightArm.roll -= 45 * 0.017453292F;
                    }
                }

                this.body.pitch -= 5F * 0.017453292F;
                this.leftLeg.pivotZ -= 1F;
                this.rightLeg.pivotZ -= 1F;
            }

            if (stand instanceof TheWorldOverHeavenEntity) {
                // Floating
                float heightOffset = 1.0f + MathHelper.sin(h / 10);
                this.head.pivotY -= heightOffset;
                this.body.pivotY -= heightOffset;

                this.leftArm.pivotY -= heightOffset;
                this.rightArm.pivotY -= heightOffset;

                // Leaning while moving
                float speedInfluence = (float) JCraftUtils.deltaPos(livingEntity).horizontalLength() * 80f * 0.017453292F;

                this.body.pitch += speedInfluence;

                this.leftLeg.pivotZ += MathHelper.sin(speedInfluence) * 12f - 2F;
                this.rightLeg.pivotZ += MathHelper.sin(speedInfluence) * 12f;

                this.rightLeg.pivotY -= heightOffset + MathHelper.sin(speedInfluence) * 6f;
                this.leftLeg.pivotY -= heightOffset + 1f + MathHelper.sin(speedInfluence) * 6f;

                this.rightLeg.pitch = speedInfluence;
                this.leftLeg.pitch = 15 * 0.017453292F + speedInfluence;

                this.leftArm.pitch *= 0.25f;
                this.rightArm.pitch *= 0.25f;

                // One arm stretched out
                if (this.leftArmPose == BipedEntityModel.ArmPose.EMPTY) {
                    this.leftArm.roll = -45 * 0.017453292F + MathHelper.sin(h / 10) / 8f;
                    this.leftArm.pitch = speedInfluence;
                }

                if (this.rightArmPose == BipedEntityModel.ArmPose.EMPTY)
                    this.rightArm.pitch = speedInfluence;
            }
        }
    }
}
