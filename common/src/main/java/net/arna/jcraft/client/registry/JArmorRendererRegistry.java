package net.arna.jcraft.client.registry;

import lombok.NonNull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Deprecated(forRemoval = true)
public class JArmorRendererRegistry {
    /**
     * Idk why the fuck AzureLib was made this way, but the boilerplate cannot be compressed into this method.
     * Creating a class that implements RenderProvider also does not work.
     * Any known attempt to reduce boilerplate causes specifically Fabric Serverside to tweak out over trying to load HumanoidModel.
     */
    /*
    @Deprecated(forRemoval = true)
    public static void createRenderer(Consumer<Object> consumer, GeoArmorRenderer<?> pRenderer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @SuppressWarnings("unchecked")
            @Override
            public @NonNull HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
                if (this.renderer == null) {
                    this.renderer = pRenderer;
                }

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }*/
}
