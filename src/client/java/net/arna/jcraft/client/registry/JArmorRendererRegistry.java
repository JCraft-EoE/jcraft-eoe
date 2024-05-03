package net.arna.jcraft.client.registry;

import net.arna.jcraft.client.renderer.armor.*;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.example.client.renderer.armor.WolfArmorRenderer;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Consumer;

public class JArmorRendererRegistry {

    public static void registerArmorRenderers() {
        GeoArmorRenderer.registerArmorRenderer(new DIOArmorRenderer(), JObjectRegistry.DIOHEADBAND,
                JObjectRegistry.DIOJACKET, JObjectRegistry.DIOPANTS, JObjectRegistry.DIOBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new JotaroArmorRenderer(), JObjectRegistry.JOTAROCAP,
                JObjectRegistry.JOTAROJACKET, JObjectRegistry.JOTAROPANTS, JObjectRegistry.JOTAROBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new KarsArmorRenderer(), JObjectRegistry.KARSHEADWRAP,
                null, null, null);
        GeoArmorRenderer.registerArmorRenderer(new StoneMaskRenderer(), JObjectRegistry.STONE_MASK,
                null, null, null);
        GeoArmorRenderer.registerArmorRenderer(new RedHatRenderer(), JObjectRegistry.RED_HAT,
                null, null, null);
        GeoArmorRenderer.registerArmorRenderer(new DIOCapeRenderer(), null,
                JObjectRegistry.DIOCAPE, null, null);



    }

    public static void createRenderer(Consumer<Object> consumer, GeoArmorRenderer<?> pRenderer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public @NotNull BipedEntityModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<LivingEntity> original) {
                if (this.renderer == null)
                    this.renderer = pRenderer;

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }
}
