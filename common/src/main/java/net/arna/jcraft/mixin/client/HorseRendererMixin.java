package net.arna.jcraft.mixin.client;

import net.arna.jcraft.JCraft;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @see HorseMarkingLayerMixin
 */
@Mixin(HorseRenderer.class)
public class HorseRendererMixin {

    @Unique
    private static final ResourceLocation EL_CONDOR_PASA = JCraft.id("textures/entity/horse/el_condor_pasa.png");
    @Unique
    private static final ResourceLocation PINKIE_PIE = JCraft.id("textures/entity/horse/pinkie_pie.png");
    @Unique
    private static final ResourceLocation SILVER_BULLET = JCraft.id("textures/entity/horse/silver_bullet.png");
    @Unique
    private static final ResourceLocation SLOW_DANCER = JCraft.id("textures/entity/horse/slow_dancer.png");
    @Unique
    private static final ResourceLocation VALKYRIE = JCraft.id("textures/entity/horse/valkyrie.png");

    // when adding more skins, also change HorseMarkingLayerMixin accordingly

    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/horse/Horse;)Lnet/minecraft/resources/ResourceLocation;", at = @At("TAIL"), cancellable = true)
    public void jcraft$getTextureLocation(final Horse entity, final CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity.hasCustomName()) {
            final String name = entity.getCustomName().getString();
            switch (name) {
                case "El Condor Pasa" -> cir.setReturnValue(EL_CONDOR_PASA);
                case "Pinkie Pie" -> cir.setReturnValue(PINKIE_PIE);
                case "Silver Bullet" -> cir.setReturnValue(SILVER_BULLET);
                case "Slow Dancer" -> cir.setReturnValue(SLOW_DANCER);
                case "Valkyrie" -> cir.setReturnValue(VALKYRIE);
            }
        }
    }

}
