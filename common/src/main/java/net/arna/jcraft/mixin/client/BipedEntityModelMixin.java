package net.arna.jcraft.mixin.client;

import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.pose.ModelType;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.client.rendering.StandUserPoseLoader;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
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

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V",
            shift = At.Shift.BEFORE))
    public void jcraft$setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch, CallbackInfo info) {
        CommonHitPropertyComponent hitProperties = JComponentPlatformUtils.getHitProperties(livingEntity);
        long endHitAnimTime = hitProperties.endHitAnimTime();
        if (endHitAnimTime > 0) {
            JClientUtils.animateHit(hitProperties.getHitAnimation(), livingEntity.isAlive() ? endHitAnimTime : 0,
                    hitProperties.getRandomRotation(), head, hat, body, rightArm, leftArm, rightLeg, leftLeg);
            return;
        }

        if (livingEntity.isHolding(JItemRegistry.FV_REVOLVER.get())) {
            AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, livingEntity.getMainArm() == HumanoidArm.RIGHT);
        }

        if (livingEntity.isHolding(JItemRegistry.PEACEMAKER.get())) {
            AnimationUtils.animateCrossbowHold(rightArm, leftArm, head, livingEntity.getMainArm() == HumanoidArm.RIGHT);
        }

        if (livingEntity.hasPose(Pose.STANDING)) {
            JClientUtils.resetPartAngles(body);

            StandUserPoseLoader.getPose(ModelType.HUMANOID, livingEntity)
                    .apply((HumanoidModel<?>) (Object) this, livingEntity, age);
        }
    }

}
