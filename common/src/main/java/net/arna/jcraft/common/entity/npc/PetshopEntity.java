package net.arna.jcraft.common.entity.npc;

import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.tickable.JEnemies;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class PetshopEntity extends PathfinderMob {

    public PetshopEntity(Level world) {
        super(JEntityTypeRegistry.PETSHOP.get(), world);
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(this);
        standData.setType(JStandTypeRegistry.HORUS.get());
        standData.setSkin(0);

        if (world.isClientSide()) return;
        JEnemies.add(this);
    }

    public static AttributeSupplier.Builder createPetshopAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FLYING_SPEED, 1.0).add(Attributes.MOVEMENT_SPEED, 0.375);
    }
    /*
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", this::animationPredicate));
        // TODO Arna
    }

    // conditions for certain animations to play (PlayState.CONTINUE)
    private PlayState animationPredicate(AnimationState<PetshopEntity> state) {
        if (state.isMoving()) {
            state.setAnimation(RawAnimation.begin().thenLoop("walk"));
        }
        else {
            state.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }

        return PlayState.CONTINUE;
    }*/
}
