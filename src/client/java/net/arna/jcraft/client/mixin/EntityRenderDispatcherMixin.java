package net.arna.jcraft.client.mixin;

import it.unimi.dsi.fastutil.Pair;
import net.arna.jcraft.client.renderer.entity.PlayerCloneRenderer;
import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    private final @Unique Map<String, PlayerCloneRenderer> cloneRenderers = new HashMap<>();

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void jcraft$shouldRender(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Entity e = entity;
        do {
            if (JUtils.shouldForceRender(e)) {
                cir.setReturnValue(true);
                return;
            }

            // Do not render PlayerCloneEntity (fated self) if it's a Time Erase clone and the user is the viewer
            if (e instanceof PlayerCloneEntity clone && JClientUtils.shouldNotRenderClone(clone)) {
                cir.cancel();
                return;
            }

            if (JUtils.shouldNotRender(e)) {
                cir.cancel();
                return;
            }

            e = e.getVehicle();
        } while (e != null);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void getCloneRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (!(entity instanceof PlayerCloneEntity clone)) return;

        Pair<Identifier, String> skin = CloneSkinTracker.getSkinFor(clone);
        cir.setReturnValue((EntityRenderer<? super T>) cloneRenderers.getOrDefault(skin.right(), cloneRenderers.get("default")));
    }

    @Inject(method = "reload", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void reloadCloneRenderers(ResourceManager manager, CallbackInfo ci, EntityRendererFactory.Context context) {
        cloneRenderers.clear();
        cloneRenderers.put("default", new PlayerCloneRenderer(context, false));
        cloneRenderers.put("slim", new PlayerCloneRenderer(context, true));
    }
}
