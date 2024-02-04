package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.projectile.BulletProjectile;
import net.arna.jcraft.common.tickable.RevolverFire;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FVRevolverItem extends Item {

    public FVRevolverItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound itemData = stack.getNbt();

        if (itemData != null && itemData.contains("Shots"))
            tooltip.add(Text.translatable("jcraft.revolver.shots").append(" §e" + itemData.get("Shots")));

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.hasStatusEffect(JStatusRegistry.DAZED))
            return TypedActionResult.fail(itemStack);
        NbtCompound data = itemStack.getOrCreateNbt();
        int shots = data.getInt("Shots");
        if (shots < 1) return TypedActionResult.fail(itemStack);
        if (!world.isClient) {
            user.getItemCooldownManager().set(JObjectRegistry.FV_REVOLVER, 4); // Unusable until fires
            RevolverFire.enqueue(new DimensionData(user, world.getRegistryKey(), 3));
        }
        return TypedActionResult.success(itemStack);
    }

    public static void fire(ItemStack itemStack, World world, LivingEntity user) {
        NbtCompound data = itemStack.getOrCreateNbt();
        int shots = data.getInt("Shots");
        if (shots < 1) return;

        data.putInt("Shots", shots - 1);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegistry.REVOLVER_FIRE, SoundCategory.PLAYERS, 1f, 1f);

        BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 2, 5);
        bullet.setVelocity(user, user.getPitch(), user.getYaw(), 0f,  10, 0F);
        world.spawnEntity(bullet);

        if (user instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(JObjectRegistry.FV_REVOLVER, 11); // Refire time
            player.incrementStat(Stats.USED.getOrCreateStat(JObjectRegistry.FV_REVOLVER));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            stack.setDamage(stack.getDamage() + 1);
            if ((stack.getMaxDamage() - stack.getDamage()) <= 0)
                stack.decrement(1);
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack s = new ItemStack(this);
        NbtCompound nbt = s.getOrCreateNbt();
        nbt.putInt("Shots", 6);
        return s;
    }
}
