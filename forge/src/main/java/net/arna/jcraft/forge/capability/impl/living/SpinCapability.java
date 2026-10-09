package net.arna.jcraft.forge.capability.impl.living;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.living.CommonSpinComponentImpl;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public class SpinCapability extends CommonSpinComponentImpl implements JCapability {

    public static Capability<SpinCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static ResourceLocation SPIN_S2C = JCraft.id("spn_s2c");

    public SpinCapability(final @NonNull LivingEntity entity) {
        super(entity);
    }

    @Override
    public void sync(final Entity entity) {
        super.sync(entity);
        if (entity instanceof ServerPlayer player) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            writeSyncPacket(buf, player);
            NetworkManager.sendToPlayer(player, SPIN_S2C, buf);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        writeToNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag) {
        readFromNbt(tag);
    }

    public static LazyOptional<SpinCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }

    public static SpinCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new SpinCapability(entity));
    }
}
