package net.arna.jcraft.common.item;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.player.SpecComponent;
import net.arna.jcraft.common.spec.SpecType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StoneMaskItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StoneMaskItem(ArmorMaterial materialIn, ArmorItem.Type slot, Settings builder) {
        super(materialIn, slot, builder);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (slot != EquipmentSlot.HEAD.getEntitySlotId()) return;

        if (entity instanceof PlayerEntity player && player.getDamageTracker().wasRecentlyAttacked()) {
            SpecComponent specComponent = JComponents.getSpecData(player);
            if (specComponent.getType() != SpecType.VAMPIRE)
                specComponent.setType(SpecType.VAMPIRE);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.stonemask.desc"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<StoneMaskItem> state) {
        Entity entity = (Entity) state.getData(DataTickets.ENTITY);
        if (entity instanceof LivingEntity livingEntity) {
            state.getController().setAnimation(RawAnimation.begin().thenLoop(livingEntity.getDamageTracker().wasRecentlyAttacked() ? "animation.stone_mask.clench" : "animation.stone_mask.dormant"));

        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {

    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return null;
    }
}
