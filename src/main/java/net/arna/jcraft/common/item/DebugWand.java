package net.arna.jcraft.common.item;

import net.arna.jcraft.client.network.s2c.ShaderActivationPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DebugWand extends Item {
    public DebugWand(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ShaderActivationPacket.send((ServerPlayerEntity) user, user, 0, 20 * 2, ShaderActivationPacket.Type.ZA_WARUDO);
        }

        return super.use(world, user, hand);
    }
}
