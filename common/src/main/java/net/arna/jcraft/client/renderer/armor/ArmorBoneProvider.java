package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneProvider;
import org.jetbrains.annotations.Nullable;

public class ArmorBoneProvider implements AzArmorBoneProvider {
    @Override
    public @Nullable AzBone getHeadBone(final AzBakedModel model) {
        return model.getBoneOrNull("helmet");
    }

    @Override
    public @Nullable AzBone getBodyBone(final AzBakedModel model) {
        return model.getBoneOrNull("chestplate");
    }

    @Override
    public @Nullable AzBone getRightArmBone(final AzBakedModel model) {
        return model.getBoneOrNull("rightArm");
    }

    @Override
    public @Nullable AzBone getLeftArmBone(final AzBakedModel model) {
        return model.getBoneOrNull("leftArm");
    }

    @Override
    public @Nullable AzBone getRightLegBone(final AzBakedModel model) {
        return model.getBoneOrNull("rightLeg");
    }

    @Override
    public @Nullable AzBone getLeftLegBone(final AzBakedModel model) {
        return model.getBoneOrNull("leftLeg");
    }

    @Override
    public @Nullable AzBone getRightBootBone(final AzBakedModel model) {
        return model.getBoneOrNull("rightBoot");
    }

    @Override
    public @Nullable AzBone getLeftBootBone(final AzBakedModel model) {
        return model.getBoneOrNull("leftBoot");
    }

    @Override
    public @Nullable AzBone getWaistBone(final AzBakedModel model) {
        return model.getBoneOrNull("waist");
    }
}
