package net.arna.jcraft.client.renderer.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.GasCanProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GasCanRenderer extends AzEntityRenderer<GasCanProjectile> {
    private static final ResourceLocation model = JCraft.id("geo/canister.geo.json");
    private static final ResourceLocation texture = JCraft.id("textures/item/gascan.png");

    public GasCanRenderer(EntityRendererProvider.Context context) {
        super(AzEntityRendererConfig.<GasCanProjectile>builder(model, texture).build(), context);
    }

    @Override
    public void render(@NotNull GasCanProjectile entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Vec3 prevVelocity = entity.getPrevDeltaMovement();
        Vec3 curVelocity = entity.getDeltaMovement();
        Vec3 velocity = new Vec3(
                Mth.lerp(partialTick, prevVelocity.x, curVelocity.x),
                Mth.lerp(partialTick, prevVelocity.y, curVelocity.y),
                Mth.lerp(partialTick, prevVelocity.z, curVelocity.z)
        );

        // Rotate the gas can to follow the velocity.
        if (velocity.lengthSqr() > 1e-5) {
            // Pitch: angle between the velocity vector and the horizontal plane
            double horizontalDist = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-velocity.y, horizontalDist)) + 90f;

            // Yaw: horizontal direction of travel
            float yaw = (float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z));

            // First, undo the rotation Minecraft already does as it will break pitch.
            poseStack.mulPose(Axis.YN.rotationDegrees(yaw));

            // Then apply pitch and re-apply yaw.
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.YN.rotationDegrees(yaw));
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }
}
