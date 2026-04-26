package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.entity.projectile.ThrownFrogProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class BluePoisonFrogItem extends Item {

    public BluePoisonFrogItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (selected && !level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 2, true, false));
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (world.isClientSide) return;

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.FROG_AMBIENT, SoundSource.PLAYERS, 1.0f,
                0.8f + world.getRandom().nextFloat() * 0.4f);

        final ThrownFrogProjectile thrown = new ThrownFrogProjectile(world, user);
        thrown.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
        thrown.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.4F, 0.5F);
        world.addFreshEntity(thrown);

        if (user instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
