package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.MatchProjectile;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MatchboxItem extends Item {
    public static final int COOLDOWN_DURATION = 5 * 20;

    public MatchboxItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                JSoundRegistry.MATCHBOX_USE.get(),
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide()) {
            RandomSource random = RandomSource.create();
            for (int i = 0; i < 8; i++) {
                MatchProjectile match = new MatchProjectile(player, level);
                match.setPos(match.position().add(
                        random.triangle(0, 0.5),
                        random.triangle(0, 0.5),
                        random.triangle(0, 0.5)
                ));
                match.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.4F, 5F);
                level.addFreshEntity(match);
            }
        }

        player.getCooldowns().addCooldown(this, COOLDOWN_DURATION);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
