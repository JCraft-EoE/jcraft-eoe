package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.BulletProjectile;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
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
        if (!world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (user.hasStatusEffect(JStatusRegister.DAZED))
                return TypedActionResult.fail(itemStack);

            NbtCompound data = itemStack.getOrCreateNbt();
            int shots = data.getInt("Shots");
            if (shots < 1)
                return TypedActionResult.fail(user.getStackInHand(hand));
            data.putInt("Shots", shots - 1);
            user.getItemCooldownManager().set(this, 8);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), JSoundRegister.REVOLVER_FIRE, SoundCategory.PLAYERS, 1f, 1f);

            BulletProjectile bullet = new BulletProjectile(world, user, 9f, 10f, 10, 5);
            bullet.setVelocity(user, user.getPitch(), user.getYaw(), 0f,  10, 0F);
            world.spawnEntity(bullet);

            //damageLogic(world, livingEntity, dir, 10, 1, false, 5, true, 4, DamageSource.mob(user), user);
        }

        return TypedActionResult.success(user.getStackInHand(hand));
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
