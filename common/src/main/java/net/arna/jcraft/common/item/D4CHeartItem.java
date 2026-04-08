package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Heart item that grants D4C (Dirty Deeds Done Dirt Cheap).
 * The only way to obtain D4C — it is removed from the stand arrow pool.
 */
public class D4CHeartItem extends Item {
    protected Set<Player> warned = Collections.newSetFromMap(new WeakHashMap<>());

    public D4CHeartItem(Properties settings) {
        super(settings);
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull final Level world, final @NonNull Player user,
                                                           @NonNull final InteractionHand hand) {
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(user);
        final StandEntity<?, ?> oldStand = standData.getStand();
        final ItemStack itemStack = user.getItemInHand(hand);

        // If player already has a stand
        if (!StandTypeUtil.isNone(standData.getType())) {
            if (!warned.contains(user)) {
                // First click: Show warning
                if (!world.isClientSide()) {
                    user.sendSystemMessage(Component.translatable("warning.jcraft.stand.change"));
                    warned.add(user);
                }
                return InteractionResultHolder.fail(itemStack);
            }
            // Second click: Proceed with stand change
        }

        // Remove 1 from item stack
        if (!user.isCreative()) {
            itemStack.shrink(1);
        }

        // 1 second usage cooldown to prevent overuse
        user.getCooldowns().addCooldown(this, 20);

        if (!world.isClientSide()) {
            warned.remove(user);

            // Remove old stand if it exists
            if (oldStand != null) {
                oldStand.desummon();
                oldStand.discard();
            }

            // Grant D4C
            standData.setType(JStandTypeRegistry.D4C.get());
            JCraft.summon(world, user);
        }

        return InteractionResultHolder.consume(itemStack);
    }
}
