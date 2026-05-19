package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodBottleItem extends Item {
    private static final int MAX_USE_TIME = 24;
    public static final float MAX_BLOOD = 16f;

    public BloodBottleItem(Properties settings) {
        super(settings);
    }

    @NonNull
    public InteractionResultHolder<ItemStack> use(@NonNull final Level world, @NonNull final Player user, @NonNull final InteractionHand hand) {
        final var result = super.use(world, user, hand);

        if (result.getResult() == InteractionResult.FAIL)
            return result;

        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        final Player playerEntity = user instanceof Player ? (Player) user : null;

        final CompoundTag nbt = stack.getOrCreateTag();

        float blood = nbt.getFloat("Blood");

        if (blood >= 0.5f) {
            CommonVampireComponent vampireComponent = JComponentPlatformUtils.getVampirism(playerEntity);

            if (vampireComponent.isVampire()) {
                boolean full = blood >= 1.0f;
                if (playerEntity != null) {
                    playerEntity.awardStat(Stats.ITEM_USED.get(this));
                    if (!playerEntity.getAbilities().instabuild && vampireComponent.getBlood() < 20) {
                        nbt.putFloat("Blood", Math.max(--blood, 0));
                    }
                }

                if (!world.isClientSide) {
                    if (playerEntity instanceof ServerPlayer) {
                        CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) playerEntity, stack);
                    }
                    vampireComponent.setBlood(vampireComponent.getBlood() + (full ? 2 : 1));
                }

                user.gameEvent(GameEvent.DRINK);
            }
        }
        return stack;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putFloat("Blood", 0);
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (stack.getOrCreateTag().getFloat("Blood") >= 0.5) {
            return UseAnim.DRINK;
        }
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("Blood")) {
            tooltip.add(Component.empty()
                    .append(Component.translatable("jcraft.blood_bottle.units"))
                    .append(Component.literal(" "))
                    .append(nbt.getFloat("Blood") + "/" + MAX_BLOOD));
        }
        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (user.getCooldowns().isOnCooldown(this) || !JUtils.canAct(user)) {
            return InteractionResult.PASS;
        }

        float bloodMult = JUtils.getBloodMult(entity);

        if (bloodMult <= 0) {
            return InteractionResult.PASS;
        }

        user.getCooldowns().addCooldown(this, 15);

        if (!user.level().isClientSide()) {
            entity.hurt(user.level().damageSources().playerAttack(user), 2);
            CompoundTag nbtCompound = stack.getOrCreateTag();

            float blood = nbtCompound.getFloat("Blood");

            float newBlood = blood + bloodMult;

            // A significantly overfilled blood bottle is probably intentionally made, so we allow it to be filled indefinitely.
            if (blood <= MAX_BLOOD * 2 && newBlood > MAX_BLOOD) {
                newBlood = MAX_BLOOD;
            }

            nbtCompound.putFloat("Blood", newBlood);

            user.setItemInHand(hand, stack);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_TIME;
    }

}
