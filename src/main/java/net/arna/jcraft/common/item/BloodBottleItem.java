package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.VampireComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class BloodBottleItem extends Item {
    private static final int MAX_USE_TIME = 16;

    public BloodBottleItem(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;

        NbtCompound nbt = stack.getOrCreateNbt();
        float blood = nbt.getFloat("Blood");

        if (blood >= 1.0f) {
            VampireComponent vampireComponent = JComponents.getVampirism(playerEntity);

            if (vampireComponent.isVampire()) {
                if (!world.isClient) {
                    if (playerEntity instanceof ServerPlayerEntity)
                        Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) playerEntity, stack);
                    vampireComponent.setBlood(vampireComponent.getBlood() + 2);
                }

                if (playerEntity != null) {
                    playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                    if (!playerEntity.getAbilities().creativeMode && vampireComponent.getBlood() < 20)
                        nbt.putFloat("Blood", --blood);
                }

                user.emitGameEvent(GameEvent.DRINK);
            }
        }
        return stack;
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.getOrCreateNbt().putFloat("Blood", 0);
        return stack;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (stack.getOrCreateNbt().getFloat("Blood") > 0)
            return UseAction.DRINK;
        return UseAction.NONE;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(this))
            return ActionResult.PASS;

        float bloodMult = JUtils.getBloodMult(entity);
        if (bloodMult <= 0)
            return ActionResult.PASS;

        user.getItemCooldownManager().set(this, 10);

        if (!user.getWorld().isClient()) {
            entity.damage(DamageSource.player(user), 2);
            NbtCompound nbtCompound = stack.getOrCreateNbt();
            nbtCompound.putFloat("Blood", nbtCompound.getFloat("Blood") + bloodMult);
            user.setStackInHand(hand, stack);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return MAX_USE_TIME;
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        appendStacks(group, (List<ItemStack>) stacks);
    }

    public static void appendStacks(ItemGroup group, List<ItemStack> stacks) {
        boolean full = group == ItemGroup.SEARCH;
        if (!full && group != JCraft.JCRAFT_GROUP) return;

        int step = 1;
        if (!full) step = 4;
        for (int i = 0; i <= 16; i += step) {
            ItemStack stack = new ItemStack(JObjectRegistry.BLOOD_BOTTLE);
            stack.getOrCreateNbt().putFloat("Blood", i);
            stacks.add(stack);
        }
    }
}
