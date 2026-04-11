package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.StandArrowEntity;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class StandArrowItem extends ArrowItem {
    // private static final DamageType DAMAGE_TYPE = new DamageType("stand_arrow", DamageScaling.NEVER, 0f, DamageEffects.HURT);
    public static final ResourceKey<DamageType> STAND_ARROW = JDamageSources.createDamageType("stand_arrow");

    protected Set<Player> warned = Collections.newSetFromMap(new WeakHashMap<>());

    public StandArrowItem(Properties settings) {
        super(settings);
    }

    public @NonNull UseAnim getUseAnimation(@NonNull final ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull final Level world, final @NonNull Player user,
                                                           @NonNull final InteractionHand hand) {
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(user);
        final StandEntity<?, ?> oldStand = standData.getStand();
        final ItemStack itemStack = user.getItemInHand(hand);

        if (!StandTypeUtil.isNone(standData.getType())) { // If the player already has a stand
            if (!warned.contains(user)) {
                if (!world.isClientSide()) {
                    user.sendSystemMessage(Component.translatable("warning.jcraft.stand.change"));
                    warned.add(user);
                }
                return InteractionResultHolder.fail(itemStack);
            }
        }

        // Remove 1 from item stack
        if (!user.isCreative()) {
            itemStack.shrink(1);
        }

        // 1 second usage cooldown to prevent overuse
        user.getCooldowns().addCooldown(this, 20);

        // damage by arrow
        int damage = Math.max(world.getGameRules().getInt(JCraft.STAND_ARROW_BASE_DAMAGE), 0);
        if (world.getDifficulty() == Difficulty.HARD) {
            damage *= 2;
        }
        else if (world.getDifficulty() == Difficulty.EASY) {
            damage /= 2;
        }
        else if (world.getDifficulty() == Difficulty.PEACEFUL) {
            damage = 0;
        }
        if (damage > 0) {
            user.hurt(JDamageSources.create(world, STAND_ARROW, user), damage);
        }

        if (!world.isClientSide()) {
            warned.remove(user);
            if (oldStand != null) {
                oldStand.desummon(); // Does any extra desummoning logic, like disabling flight
                oldStand.discard(); // Actually removes the stand
            }

            // Roll for stand (can't roll the same one twice)
            final RandomSource random = RandomSource.create();
            final StandType oldType = standData.getType();
            StandType newType;
            do {
                newType = StandTypeUtil.getRandomRegular(random);
            } while (newType == oldType);

            standData.setType(newType);
            JCraft.summon(world, user);

            user.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public @NonNull AbstractArrow createArrow(@NonNull final Level level, @NonNull final ItemStack stack, @Nullable final LivingEntity shooter) {
        if (!(shooter instanceof ServerPlayer)) stack.shrink(1);
        return new StandArrowEntity(shooter, level);
    }
}
