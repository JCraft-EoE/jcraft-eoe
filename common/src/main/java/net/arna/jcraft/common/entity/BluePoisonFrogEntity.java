package net.arna.jcraft.common.entity;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BluePoisonFrogEntity extends Frog {

    public static final int LIFETIME = 500;

    private int timeToLive = LIFETIME;
    private LivingEntity master;

    public BluePoisonFrogEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public void setMaster(LivingEntity master) {
        this.master = master;
    }

    public LivingEntity getMaster() {
        return master;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        if (onGround()) {
            poisonNearby();
            kill();
            return;
        }

        if (--timeToLive < 1) {
            kill();
            return;
        }

        if (tickCount % 10 == 0) {
            poisonNearby();
        }
    }

    private void poisonNearby() {
        final AABB touchBox = getBoundingBox().inflate(0.6);
        level().getEntitiesOfClass(LivingEntity.class, touchBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != master)
        ).forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2, true, true)));
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;

        if (!player.getAbilities().instabuild) {
            final ItemStack frogStack = new ItemStack(JItemRegistry.BLUE_POISON_FROG.get());
            player.addItem(frogStack);
        }
        discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
            discard();
            return true;
        }
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Frog.createAttributes();
    }
}
