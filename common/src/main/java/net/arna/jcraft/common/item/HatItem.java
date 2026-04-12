package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HatItem extends ArmorItem {
    //private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    //private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public HatItem(ArmorMaterial materialIn, Properties builder) {
        super(materialIn, Type.HELMET, builder);
    }

    @Override
    public boolean isValidRepairItem(@NonNull ItemStack stack, ItemStack ingredient) {
        if (ingredient.is(Items.LEATHER)) {
            return true;
        }
        return super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @Nullable Level world, @NonNull List<Component> tooltip, @NonNull TooltipFlag context) {
        if (getDefaultInstance().is(JTagRegistry.PROTECTS_FROM_SUN)) {
            tooltip.add(Component.translatable("jcraft.sunprotection.desc"));
        }
        super.appendHoverText(stack, world, tooltip, context);
    }

    /*
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::predicate));
    }

    private PlayState predicate(AnimationState<HatItem> animationState) {
        return PlayState.STOP;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @SuppressWarnings("unchecked")
            @Override
            public @NonNull HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
                if (this.renderer == null) {
                    if (itemStack.is(JItemRegistry.KARS_HEADWRAP.get())) {
                        this.renderer = new KarsArmorRenderer();
                    }
                    if (itemStack.is(JItemRegistry.RED_HAT.get())) {
                        this.renderer = new RedHatRenderer();
                    }
                    if (itemStack.is(JItemRegistry.PUCCIS_HAT.get())) {
                        this.renderer = new PuccisHatRenderer();
                    }
                }

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }*/
}
