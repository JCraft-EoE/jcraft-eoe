package net.arna.jcraft.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KnifeItem extends Item {
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public KnifeItem(Properties settings) {
        super(settings);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 4.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.2, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (user.hasEffect(JStatusRegistry.DAZED.get())) {
            return InteractionResultHolder.fail(stack);
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    protected float getSpeedMult(ItemStack stack, int remainingUseTicks) {
        float speedMult = (getUseDuration(stack) - remainingUseTicks);
        if (speedMult > getChargeTime()) {
            speedMult = getChargeTime();
        }
        speedMult /= getChargeTime();
        return speedMult;
    }

    protected float getChargeTime() {
        return 10F;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("jcraft.knife.chargetime").append(" §e" + getChargeTime() / 20F + "§9s"));
        super.appendHoverText(stack, world, tooltip, context);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user.hasEffect(JStatusRegistry.DAZED.get())) {
            return;
        }

        if (!world.isClientSide) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCooldowns().addCooldown(this, 15);
                serverPlayer.awardStat(Stats.ITEM_USED.get(this));
                if (!serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            KnifeProjectile knife = new KnifeProjectile(world, user);
            knife.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F * getSpeedMult(stack, remainingUseTicks), 0F);
            world.addFreshEntity(knife);
        } else {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
        }
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return !miner.isCreative();
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(Blocks.COBWEB);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
