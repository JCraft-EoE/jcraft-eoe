package net.arna.jcraft.client.renderer.entity.stands;

import lombok.NonNull;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneProvider;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.MandomEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class MandomRenderer extends AbstractExoskeletonRenderer<MandomEntity> {

    private static final AzArmorBoneProvider BONE_PROVIDER = new AzArmorBoneProvider() {
        @Override public AzBone getHeadBone(AzBakedModel m)     { return null; }
        @Override public AzBone getBodyBone(AzBakedModel m)     { return m.getBoneOrNull("body"); }
        @Override public AzBone getRightArmBone(AzBakedModel m) { return null; }
        @Override public AzBone getLeftArmBone(AzBakedModel m)  { return null; }
        @Override public AzBone getRightLegBone(AzBakedModel m) { return null; }
        @Override public AzBone getLeftLegBone(AzBakedModel m)  { return null; }
        @Override public AzBone getRightBootBone(AzBakedModel m){ return null; }
        @Override public AzBone getLeftBootBone(AzBakedModel m) { return null; }
        @Override public AzBone getWaistBone(AzBakedModel m)    { return null; }
    };

    public MandomRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, b -> b.setRenderType(renderType(RenderType::entityTranslucentCull)),
                JStandTypeRegistry.MANDOM.get(), false, false, 0f, 0f, 1f, BONE_PROVIDER);
    }

}
