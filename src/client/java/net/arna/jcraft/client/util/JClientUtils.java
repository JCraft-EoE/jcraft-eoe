package net.arna.jcraft.client.util;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.component.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.DimValues;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.processor.IBone;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.common.util.JUtils.RAD_TO_DEG;
import static net.arna.jcraft.common.util.JUtils.deltaPos;

public class JClientUtils {

    // Timestop tracking
    public static List<DimValues> activeTimestops = new ArrayList<>();

    public static void removeTimestop(int timestopperId) {
        for (DimValues timestop : activeTimestops) {
            Entity timestopper = timestop.user;
            if (timestopper.getId() != timestopperId) continue;
            activeTimestops.remove(timestop);
            return;
        }
    }

    public static boolean isInTSRange(Vec3d pos) {
        for (DimValues timeStop : activeTimestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return true;
        return false;
    }

    public static boolean isInTSRange(BlockPos pos) {
        for (DimValues timeStop : activeTimestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return true;
        return false;
    }

    public static int getTicksIfInTSRange(BlockPos pos) {
        for (DimValues timeStop : activeTimestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return timeStop.timer;
        return 0;
    }

    // Torso/Head rotation for stands
    public static void animateGenericHumanoid(StandEntityModel<?> model, StandEntity<?, ?> entity, LivingEntity player, float partialTick) {
        animateGenericHumanoid(model, entity, player, partialTick, false, false);
    }

    public static void animateGenericHumanoid(StandEntityModel<?> model, StandEntity<?, ?> entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead) {
        animateGenericHumanoid(model, entity, player, partialTick, flipBody, flipHead, 0, 0, 90f);
    }

    public static void animateGenericHumanoid(StandEntityModel<?> model, StandEntity<?, ?> entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead, float tPO, float hPO) {
        animateGenericHumanoid(model, entity, player, partialTick, flipBody, flipHead, tPO, hPO, 90f);
    }

    public static void animateGenericHumanoid(StandEntityModel<?> model, StandEntity<?, ?> entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead, float tPO, float hPO, float velInfluence) {
        float overVel = 0;

        if (entity.getMoveStun() < 1) {
            Vec3d playerVel = deltaPos(player);
            overVel = MathHelper.clamp((float) playerVel.horizontalLength() - 0.05f, -1f, 1f);

            // If going backwards
            if (playerVel.normalize().add(entity.getRotationVector()).horizontalLengthSquared() < playerVel.normalize().horizontalLengthSquared())
                velInfluence *= -1;

            IBone torso = model.getAnimationProcessor().getBone("torso");
            if (torso != null) {
                float pitch = (180f + overVel * velInfluence) * 3.1415f / 180f;
                if (flipBody) {
                    pitch += 3.1415f;
                    pitch = -pitch;
                }
                torso.setRotationX(pitch + tPO);
            }
        }

        if (entity.isBlocking() || entity.isIdle()) { // if in/going to idle, or blocking
            IBone head = model.getAnimationProcessor().getBone("head");
            if (head != null) {
                float headPitch = (player.getPitch() - overVel * velInfluence) * 3.1415f / 180f;
                if (flipHead) headPitch = -headPitch;
                head.setRotationX(headPitch + hPO);
            }
        }
    }

    public static boolean shouldForceRender(Entity entity) {
        if (entity instanceof D4CEntity d4c && d4c.getState() == D4CEntity.State.FLAG ||
            entity instanceof KingCrimsonEntity kc && kc.getTETime() > 0 && kc.getUser() == MinecraftClient.getInstance().player)
            return true;
        return entity instanceof CreamEntity cream && cream.isHalfBall();
    }

    public static boolean shouldNotRender(Entity entity) {
        Entity passenger = entity.getFirstPassenger();
        return passenger instanceof KingCrimsonEntity kc && kc.getTETime() > 0 ||
                passenger instanceof D4CEntity d4c && d4c.getState() == D4CEntity.State.FLAG ||
                passenger instanceof CreamEntity cream && cream.isHalfBall();
    }

    public static void resetPartAngles(ModelPart part) {
        ModelTransform defaultTransform = part.getDefaultTransform();
        part.pitch = defaultTransform.pitch;
        part.yaw = defaultTransform.yaw;
        part.roll = defaultTransform.roll;
    }

    public static void animateHit(HitPropertyComponent.HitAnimation hitAnimation, long endHitAnimTime, Vec3d randomRotation, ModelPart head, ModelPart hat, ModelPart body, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg) {
        if (endHitAnimTime > 20L)
            endHitAnimTime = 20L;
        float angDegrees = endHitAnimTime * RAD_TO_DEG;

        if (endHitAnimTime <= 1) {
            leftLeg.resetTransform();
            rightLeg.resetTransform();
            resetPartAngles(body);
        } else {
            body.yaw = (float) (randomRotation.x * angDegrees * 0.35);
            body.roll = (float) (randomRotation.z * angDegrees * 0.35);
        }

        if (endHitAnimTime == 0) // If dead
            return;

        switch (hitAnimation) {
            case HIGH -> {
                head.pitch += angDegrees;

                body.pitch -= angDegrees;

                leftLeg.pivotZ -= endHitAnimTime * 0.25F;
                rightLeg.pivotZ -= endHitAnimTime * 0.25F;

                rightArm.roll += angDegrees;
                leftArm.roll -= angDegrees;
            }
            case MID -> {
                head.pitch += angDegrees;

                body.pitch += angDegrees;

                leftLeg.pivotZ += endHitAnimTime * 0.25F;
                rightLeg.pivotZ += endHitAnimTime * 0.25F;
                leftLeg.pivotY -= endHitAnimTime * 0.175F;
                rightLeg.pivotY -= endHitAnimTime * 0.175F;

                rightLeg.pitch -= angDegrees;
                leftLeg.pitch -= angDegrees;
            }
            case LOW -> {
                head.pitch += angDegrees;

                body.pitch += angDegrees;

                leftLeg.pivotZ += endHitAnimTime * 0.175F;
                rightLeg.pivotZ += endHitAnimTime * 0.175F;
                leftLeg.pivotY -= endHitAnimTime * 0.0875F;
                rightLeg.pivotY -= endHitAnimTime * 0.0875F;

                rightLeg.pitch += angDegrees;
                leftLeg.pitch += angDegrees;

                rightArm.roll += angDegrees;
                leftArm.roll -= angDegrees;
            }
            case CRUSH -> {
                body.pitch += angDegrees;

                leftLeg.pivotZ += endHitAnimTime * 0.25F;
                rightLeg.pivotZ += endHitAnimTime * 0.25F;
                leftLeg.pivotY -= endHitAnimTime * 0.175F;
                rightLeg.pivotY -= endHitAnimTime * 0.175F;

                angDegrees *= 1.75F;

                head.pitch += MathHelper.sin(endHitAnimTime * 0.1F);

                rightArm.roll += angDegrees;
                leftArm.roll -= angDegrees;
                rightLeg.pitch -= angDegrees;
                leftLeg.pitch -= angDegrees;
            }
            case LAUNCH -> {
                //angDegrees *= 4.0F;

                head.pitch += angDegrees;

                body.pitch += angDegrees;

                leftLeg.pivotZ += endHitAnimTime * 0.125F;
                rightLeg.pivotZ += endHitAnimTime * 0.125F;
                leftLeg.pivotY -= endHitAnimTime * 0.125F;
                rightLeg.pivotY -= endHitAnimTime * 0.125F;

                rightArm.roll += angDegrees;
                leftArm.roll -= angDegrees;
                rightLeg.pitch -= angDegrees;
                leftLeg.pitch -= angDegrees;
            }
            case ROLL -> {

            }
        }
    }
}
