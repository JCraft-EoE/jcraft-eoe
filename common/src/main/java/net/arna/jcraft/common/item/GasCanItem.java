package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.projectile.GasCanProjectile;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GasCanItem extends Item {
    public static final int COOLDOWN_DURATION = 5 * 20;

    public GasCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 7200;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.hasEffect(JStatusRegistry.DAZED.get())) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user, int timeCharged) {
        if (user.hasEffect(JStatusRegistry.DAZED.get())) {
            return;
        }

        // Fix for a Minecraft bug where this method does get called on the client,
        // but not on the server when you switch to a different item the moment you release.
        // The actual selected item stack is already a different one, but this method still gets called.
        if (user.getItemInHand(user.getUsedItemHand()) != stack) return;

        level.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                JSoundRegistry.GAS_CAN_TOSS.get(),
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide()) {
            GasCanProjectile projectile = new GasCanProjectile(user, level);
            projectile.setItem(stack);
            projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, getSpeedMult(stack, timeCharged), 1.0F);
            level.addFreshEntity(projectile);
        }

        if (!(user instanceof Player player)) return;
        player.getCooldowns().addCooldown(this, COOLDOWN_DURATION);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private float getSpeedMult(ItemStack stack, int remainingUseTicks) {
        float speedMult = (getUseDuration(stack) - remainingUseTicks);
        if (speedMult > getChargeTime()) {
            speedMult = getChargeTime();
        }
        speedMult /= getChargeTime();
        return speedMult;
    }

    private float getChargeTime() {
        return 20f;
    }
}
