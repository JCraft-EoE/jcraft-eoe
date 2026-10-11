package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.client.renderer.entity.PlayerCloneRenderer;
import net.arna.jcraft.client.rendering.CloneSkinTracker;
import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);

    private final @Unique Map<String, PlayerCloneRenderer> jcraft$cloneRenderers = new HashMap<>();

    @WrapMethod(method = "shouldRender")
    private <E extends Entity> boolean jcraft$shouldRender(final E entity, final Frustum frustum, final double camX, final double camY, final double camZ, final Operation<Boolean> original) {
        Entity e = entity;
        do {
            if (JClientUtils.shouldForceRender(e)) {
                return true;
            }

            // Do not render PlayerCloneEntity (fated self) if it's a Time Erase clone and the user is the viewer
            if (e instanceof PlayerCloneEntity clone && !getRenderer(clone).shouldRender(clone, frustum, camX, camY, camZ)) {
                return false;
            }

            if (JClientUtils.shouldNotRender(e)) {
                return false;
            }

            e = e.getVehicle();
        } while (e != null);

        return original.call(entity, frustum, camX, camY, camZ);
    }

    @SuppressWarnings("unchecked")
    @WrapMethod(method = "getRenderer")
    private <T extends Entity> EntityRenderer<? super T> getCloneRenderer(final T entity, final Operation<EntityRenderer<? super T>> original) {
        if (!(entity instanceof PlayerCloneEntity clone)) {
            return original.call(entity);
        }
        return (EntityRenderer<? super T>) jcraft$cloneRenderers.getOrDefault(CloneSkinTracker.getModelFor(clone),
                jcraft$cloneRenderers.get("default"));
    }

    @ModifyExpressionValue(method = "onResourceManagerReload", at = @At(value = "NEW", target = "(Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;Lnet/minecraft/client/renderer/entity/ItemRenderer;Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/model/geom/EntityModelSet;Lnet/minecraft/client/gui/Font;)Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;"))
    private EntityRendererProvider.Context reloadCloneRenderers(final EntityRendererProvider.Context original) {
        jcraft$cloneRenderers.clear();
        jcraft$cloneRenderers.put("default", new PlayerCloneRenderer(original, false));
        jcraft$cloneRenderers.put("slim", new PlayerCloneRenderer(original, true));
        return original;
    }

}
