package net.arna.jcraft.forge.capability.impl.living;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.living.CommonHamonComponentImpl;
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

public class HamonCapability extends CommonHamonComponentImpl implements JCapability {

    public static ResourceLocation HAMON_S2C = JCraft.id("hmn_s2c");
    public static Capability<HamonCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public HamonCapability(LivingEntity living) {
        super(living);
    }

    @Override
    public void sync(Entity entity) {
        super.sync(entity);
        if (entity instanceof ServerPlayer player) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            writeSyncPacket(buf, player);
            NetworkManager.sendToPlayer(player, HAMON_S2C, buf);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.writeToNbt(tag);
        return tag;
    }
    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.readFromNbt(tag);
    }

    public static LazyOptional<HamonCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
    public static HamonCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new HamonCapability(entity));
    }

}
