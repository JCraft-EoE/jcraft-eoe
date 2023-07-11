package net.arna.jcraft.client.util;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.ISpec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.arna.jcraft.common.util.JUtils.deltaPos;

public class JClientUtils {
    public static JCraftSpec getSpec(PlayerEntity player) {
        return ((ISpec)player).getSpec();
    }

    // Timestop tracking
    public static List<DimValues> activeTimestops = new ArrayList<>();
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
    public static void animateGenericHumanoid(AnimatedTickingGeoModel<? extends StandEntity> model, StandEntity entity, LivingEntity player, float partialTick) {
        animateGenericHumanoid(model, entity, player, partialTick, false, false);
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel<? extends StandEntity> model, StandEntity entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead) {
        animateGenericHumanoid(model, entity, player, partialTick, flipBody, flipHead, 0, 0, 90f);
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel<? extends StandEntity> model, StandEntity entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead, float tPO, float hPO) {
        animateGenericHumanoid(model, entity, player, partialTick, flipBody, flipHead, tPO, hPO, 90f);
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel<? extends StandEntity> model, StandEntity entity, LivingEntity player, float partialTick, boolean flipBody, boolean flipHead, float tPO, float hPO, float velInfluence) {
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

        if (entity.getState() == 3 || entity.getState() < 2) { // if in/going to idle, or blocking
            IBone head = model.getAnimationProcessor().getBone("head");
            if (head != null) {
                float headPitch = (player.getPitch() - overVel * velInfluence) * 3.1415f / 180f;
                if (flipHead) headPitch = -headPitch;
                head.setRotationX(headPitch + hPO);
            }
        }
    }

    public static boolean shouldNotRenderClone(PlayerCloneEntity clone) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // It's recommended to use ((IEntityDataSaver)player).getStand(), but in this case it doesn't matter
        if (player != null && player.getFirstPassenger() instanceof KingCrimsonEntity) {
            UUID masterId = clone.getMasterId();
            if (masterId == null) return false;
            return masterId.equals(player.getUuid());
        }
        return false;
    }
}
