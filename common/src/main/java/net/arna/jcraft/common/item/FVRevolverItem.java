package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FVRevolverItem extends Item {

    public FVRevolverItem(Properties settings) {
        super(settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag itemData = stack.getTag();

        if (itemData != null && itemData.contains("Shots")) {
            tooltip.add(Component.translatable("jcraft.revolver.shots").append(" §e" + itemData.get("Shots")));
        }

        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (user.hasEffect(JStatusRegistry.DAZED.get()) || user.isSpectator()) {
            return InteractionResultHolder.fail(itemStack);
        }
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt("Shots");
        if (shots < 1) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (!world.isClientSide) {
            user.getCooldowns().addCooldown(JItemRegistry.FV_REVOLVER.get(), 4); // Unusable until fires
            //RevolverFire.enqueue(new DimensionData(user, world.dimension(), 3));
            fire(itemStack, world, user);
        }
        return InteractionResultHolder.success(itemStack);
    }

    public static void fire(ItemStack itemStack, Level world, LivingEntity user) {
        CompoundTag data = itemStack.getOrCreateTag();
        int shots = data.getInt("Shots");
        if (shots < 1) {
            return;
        }

        data.putInt("Shots", shots - 1);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegistry.REVOLVER_FIRE.get(), SoundSource.PLAYERS, 1f, 1f);

        BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 2, 5);
        bullet.shootFromRotation(user, user.getXRot(), user.getYRot(), 0f, 10, 0F);
        world.addFreshEntity(bullet);

        if (user instanceof Player player) {
            player.getCooldowns().addCooldown(JItemRegistry.FV_REVOLVER.get(), 11); // Refire time
            player.awardStat(Stats.ITEM_USED.get(JItemRegistry.FV_REVOLVER.get()));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide()) {
            stack.setDamageValue(stack.getDamageValue() + 1);
            if ((stack.getMaxDamage() - stack.getDamageValue()) <= 0) {
                stack.shrink(1);
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack s = new ItemStack(this);
        CompoundTag nbt = s.getOrCreateTag();
        nbt.putInt("Shots", 6);
        return s;
    }
}
