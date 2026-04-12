package net.arna.jcraft.client.renderer.entity.npc;

import lombok.NonNull;
import net.arna.jcraft.client.renderer.entity.AbstractEntityRenderer;
import net.arna.jcraft.common.entity.npc.TonpettyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * The {@link AbstractEntityRenderer} for {@link TonpettyEntity}
 */
@Environment(EnvType.CLIENT)
public class TonpettyRenderer extends AbstractEntityRenderer<TonpettyEntity> {

    public static final String ID = "tonpetty";

    public TonpettyRenderer(final @NonNull EntityRendererProvider.Context context) {
        super(context, TonpettyAnimator::new, ID);
    }

    public static class TonpettyAnimator extends EntityAnimator<TonpettyEntity> {
        public TonpettyAnimator() {
            super(ID);
        }

        @Override
        public void setCustomAnimations(final TonpettyEntity animatable, final float partialTicks) {
            super.setCustomAnimations(animatable, partialTicks);
            context().boneCache().getBakedModel().getBone("head").ifPresent(head -> {
                head.setRotX(-animatable.getXRot() * Mth.DEG_TO_RAD);
                head.setRotY((animatable.getYRot() - animatable.getViewYRot(partialTicks)) * Mth.DEG_TO_RAD);
            });
        }
    }

}
