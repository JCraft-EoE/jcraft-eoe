package net.arna.jcraft.client.renderer.entity.npc;

import lombok.NonNull;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.AyaTsujiEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * The {@link AbstractEntityRenderer} for {@link AyaTsujiEntity}
 */
@Environment(EnvType.CLIENT)
public class AyaTsujiRenderer extends AbstractEntityRenderer<AyaTsujiEntity> {

    public static final String ID = "aya_tsuji";

    public AyaTsujiRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, AyaTsujiAnimator::new, ID);
    }

    public static class AyaTsujiAnimator extends EntityAnimator<AyaTsujiEntity> {
        public AyaTsujiAnimator() {
            super(ID);
        }

        @Override
        public void setCustomAnimations(final AyaTsujiEntity animatable, final float partialTicks) {
            super.setCustomAnimations(animatable, partialTicks);
            context().boneCache().getBakedModel().getBone("head").ifPresent(head -> {
                head.setRotX(-animatable.getXRot() * Mth.DEG_TO_RAD);
                head.setRotY((animatable.getYRot() - animatable.getViewYRot(partialTicks)) * Mth.DEG_TO_RAD);
            });
        }
    }

}
