package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.GasCanProjectile;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GasCanItem extends Item {
    public GasCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                JSoundRegistry.GAS_CAN_TOSS.get(),
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            GasCanProjectile projectile = new GasCanProjectile(player, level);
            projectile.setItem(itemStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
