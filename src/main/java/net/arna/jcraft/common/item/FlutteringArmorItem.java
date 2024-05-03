package net.arna.jcraft.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import software.bernie.example.item.WolfArmorItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * {@link ArmorItem} animated by GeckoLib to flutter when its wearer is moving.
 */
public class FlutteringArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FlutteringArmorItem(ArmorMaterial materialIn, ArmorItem.Type slot, Settings builder) {
        super(materialIn, slot, builder);
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        LivingEntity livingEntity = event.getExtraDataOfType(LivingEntity.class).get(0);
        boolean moving = (livingEntity instanceof PlayerEntity player) ?
                !JUtils.deltaPos(player).equals(Vec3d.ZERO) :
                livingEntity.getVelocity().horizontalLengthSquared() > 0.01;
        event.getController().setAnimation(new AnimationBuilder().loop(
                moving ? "animation.moving" : "animation.idle"));
        return PlayState.CONTINUE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(this, "controller", 10, this::predicate));
    }

    private PlayState predicate(AnimationState state) {
        Entity entity = (Entity) state.getData(DataTickets.ENTITY);
        state.getController().setAnimation(RawAnimation.begin().thenLoop(
                entity.getVelocity().horizontalLengthSquared() > 0.01 ? "animation.jotarooutfit.moving" : "animation.jotarooutfit.idle"));
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
