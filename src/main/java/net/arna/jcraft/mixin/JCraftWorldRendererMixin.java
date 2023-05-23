package net.arna.jcraft.mixin;

import net.arna.jcraft.util.ITimeStop;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

@Mixin(WorldRenderer.class)
public class JCraftWorldRendererMixin {
    @Shadow
    @Final
    private ClientWorld world;

    @ModifyArgs(
            method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void jcraft$deltaTick(Args args) {
        Entity entity = args.get(0);
        if (((ITimeStop)entity).getTimeStopTicks() > 0) { args.set(5, 0.0F); } // Args 0 = ent, 5 = deltatick
    }
}
