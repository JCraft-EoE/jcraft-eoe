package net.arna.jcraft.fabric.mixin.client;

import net.arna.jcraft.client.util.JClientUtils;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.arna.jcraft.client.util.JClientUtils.timestopTimestamps;

//@Mixin(GeoEntity.class)
public interface AzurelibTimestopRenderingMixin {
    /*@Inject(method = "getTick", at = @At("HEAD"), cancellable = true, remap = false)
    default void jcraft$stopTimeTick(Object entity, CallbackInfoReturnable<Double> cir) {
        final Entity ent = (Entity)entity;

        if (JUtils.isAffectedByTimeStop(ent) && JClientUtils.isInTSRange(ent)) {
            if (!timestopTimestamps.containsKey(ent)) timestopTimestamps.put(ent, RenderUtils.getCurrentTick());
            cir.setReturnValue(timestopTimestamps.get(ent));
        } else {
            timestopTimestamps.remove(ent);
        }
    }*/
}
