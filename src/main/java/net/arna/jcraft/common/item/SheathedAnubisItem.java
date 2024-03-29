package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SheathedAnubisItem extends SpecObtainmentItem {
    public SheathedAnubisItem(Settings settings, SpecType spec) {
        super(settings, spec);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.anubis.namedesc"));
        tooltip.add(Text.translatable("jcraft.anubis.bloodthirstdesc"));
        tooltip.add(Text.translatable("jcraft.anubis.removaldesc"));
        tooltip.add(Text.translatable("jcraft.sheathedanubis.blockdesc"));
        tooltip.add(Text.translatable("jcraft.sheathedanubis.desc"));
        super.appendTooltip(stack, world, tooltip, context); // Doubles the previous Anubis description
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user.isSneaking()) { // Block
            user.setCurrentHand(hand);
        } else { // Unsheathe
            if (world.isClient) return TypedActionResult.fail(itemStack);
            StandEntity<?, ?> stand = JUtils.getStand(user);
            if (stand != null && stand.blocking) return TypedActionResult.fail(itemStack);
            ServerWorld serverWorld = (ServerWorld)world;

            boolean specChanged = tryGetSpec(user);
            if (specChanged) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE, serverWorld, user.getPos());
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_SPECCHANGE, serverWorld, user.getPos());
                user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBIS));
            } else if (!warned) {
                JUtils.serverPlaySound(JSoundRegistry.ANUBIS_UNSHEATHE, serverWorld, user.getPos());
                user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBIS));
            }
        }

        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient) return;
        if (entity instanceof PlayerEntity player) // Bloodlust
            AnubisItem.handleAnubisEffects(player.getLastAttackTime() - player.age, player);
    }
}
