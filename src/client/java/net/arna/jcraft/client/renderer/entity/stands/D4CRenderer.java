package net.arna.jcraft.client.renderer.entity.stands;

import net.arna.jcraft.client.model.entity.StandEntityModel;
import net.arna.jcraft.common.entity.D4CEntity;
import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;

public class D4CRenderer extends ExtendedGeoEntityRenderer<D4CEntity> {

    protected ItemStack mainHandItem;
    protected ItemStack offHandItem;

    public D4CRenderer(EntityRendererFactory.Context context) {
        super(context, new StandEntityModel<>(StandType.D4C));
    }

    @Override
    public RenderLayer getRenderType(D4CEntity animatable, float partialTicks, MatrixStack stack,
                                     @Nullable VertexConsumerProvider renderTypeBuffer, @Nullable VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {

        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (mcClient.player.getFirstPassenger() == animatable)
                return RenderLayer.getEntityNoOutline(this.getTextureLocation(animatable));

        return RenderLayer.getEntityCutout(this.getTextureLocation(animatable));
    }

    // Adds ability to change render alpha
    @Override
    public void render(GeoModel model, D4CEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        float a = 1f;
        MinecraftClient mcClient = MinecraftClient.getInstance();
        if (mcClient.options.getPerspective().isFirstPerson() && mcClient.player != null)
            if (JUtils.getStand(mcClient.player) == animatable )
                a = animatable.getAlphaOverride();
        float gR = 1.0f - a;

        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green - gR, blue, a);
    }

    /*
        /execute as @e[type=jcraft:d4c] run data merge entity @s {HandItems:[{id:"jcraft:fv_revolver", Count:1b},{id:"jcraft:fv_revolver", Count:1b}]}
     */
    protected float partialTick = 0f;
    @Override
    public void renderEarly(D4CEntity animatable, MatrixStack poseStack, float partialTick, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float partialTicks) {
        this.mainHandItem = animatable.getEquippedStack(EquipmentSlot.MAINHAND);
        this.offHandItem = animatable.getEquippedStack(EquipmentSlot.OFFHAND);
        this.partialTick = partialTick;

        super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, partialTicks);
    }

    @Override
    protected ItemStack getHeldItemForBone(String boneName, D4CEntity currentEntity) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? mainHandItem : offHandItem;
            case DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    currentEntity.isLeftHanded() ? offHandItem : mainHandItem;
            default -> null;
        };
    }

    @Override
    protected ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return switch (boneName) {
            case DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT, DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT ->
                    ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND; // Do Defaults
            default -> ModelTransformation.Mode.NONE;
        };
    }

    @Override
    protected void preRenderItem(MatrixStack stack, ItemStack item, String boneName, D4CEntity currentEntity, IBone bone) {
        //todo: fix d4c revolver rotation; a hack is currently implemented due to something (sodium?) breaking hand rotation for d4c
        float ang = -90f;
        D4CEntity.State state = currentEntity.getState();
        if (state == D4CEntity.State.THROW || state == D4CEntity.State.GIVE_GUN)
            ang += (currentEntity.getMoveStun() + 1f - this.partialTick) * 65f;
        stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(ang));
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Override
    protected Identifier getTextureForBone(String boneName, D4CEntity currentEntity) {
        return null;
    }

    @Override
    protected BlockState getHeldBlockForBone(String boneName, D4CEntity currentEntity) {
        return null;
    }

    @Override
    protected void postRenderItem(MatrixStack PoseStack, ItemStack item, String boneName, D4CEntity currentEntity, IBone bone) {
    }

    @Override
    protected void preRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, D4CEntity currentEntity) {
    }

    @Override
    protected void postRenderBlock(MatrixStack PoseStack, BlockState block, String boneName, D4CEntity currentEntity) {
    }
}
