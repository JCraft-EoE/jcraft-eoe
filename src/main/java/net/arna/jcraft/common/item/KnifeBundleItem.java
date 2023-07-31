package net.arna.jcraft.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class KnifeBundleItem extends KnifeItem {
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public KnifeBundleItem(Settings settings) {
        super(settings);

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 6.0, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.8, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    protected float getChargeTime() {
        return 20F;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user.hasStatusEffect(JStatusRegistry.DAZED))
            return;

        if (!world.isClient) {
            float speedMult = getSpeedMult(stack, remainingUseTicks);

            if (user instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.getItemCooldownManager().set(this, 60);
                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(this));
                if (!serverPlayer.getAbilities().creativeMode) stack.decrement(1);
            }

            Random random = Random.create();
            for (int i = 0; i < 9; i++) {
                KnifeProjectile knife = new KnifeProjectile(world, user);
                knife.setPosition(knife.getPos().add(
                        random.nextTriangular(0, 0.5),
                        random.nextTriangular(0, 0.5),
                        random.nextTriangular(0, 0.5)
                ));
                knife.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F * speedMult, 5F);
                world.spawnEntity(knife);
            }
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }
}
