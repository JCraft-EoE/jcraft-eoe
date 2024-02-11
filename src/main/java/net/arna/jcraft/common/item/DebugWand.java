package net.arna.jcraft.common.item;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DebugWand extends Item {
    public DebugWand(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));

        if (user.isSneaking()) {
            world.playSound(null, user.getBlockPos(), JSoundRegistry.TW_TS_CLEAN, SoundCategory.PLAYERS, 1.2f, 1);
            ShaderActivationPacket.send((ServerPlayerEntity) user, user, 0, 20 * 6, ShaderActivationPacket.Type.ZA_WARUDO);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getWorld().isClient) return ActionResult.PASS;

        // Feel free to remove or modify these to debug other components/features.
        if (player.isSneaking())
            ShaderActivationPacket.send((ServerPlayerEntity) player, player, 0, 20 * 6, ShaderActivationPacket.Type.CRIMSON);
        else
            JComponents.getShockwaveHandler(context.getWorld()).addShockwave(context.getHitPos(), player.getRotationVector());

        return super.useOnBlock(context);
    }
}
