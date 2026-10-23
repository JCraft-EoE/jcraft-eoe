package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.client.rendering.ModelWithCustomRenderer;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelShaperMixin {

    @WrapMethod(method = "getItemModel(Lnet/minecraft/world/item/Item;)Lnet/minecraft/client/resources/model/BakedModel;")
    private BakedModel overrideStandDiscModel(final Item item, final Operation<BakedModel> original) {
        // Ensure that stand and spec discs are rendered using a custom renderer
        if (item == JItemRegistry.STAND_DISC.get() || item == JItemRegistry.SPEC_DISC.get()) {
            return new ModelWithCustomRenderer(original.call(item));
        }
        return original.call(item);
    }

}
