package net.arna.jcraft.forge.capability.impl.entity;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.entity.CommonGrabComponentImpl;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public class GrabCapability extends CommonGrabComponentImpl implements JCapability {

    public static ResourceLocation GRAB_S2C = JCraft.id("grab_s2c");

    public static Capability<GrabCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public GrabCapability(Entity entity) {
        super(entity);
    }

    @Override
    public void sync(Entity entity) {
        // super.sync(entity);
        if (entity.level() instanceof ServerLevel serverWorld) {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(entity.getId());
            writeSyncPacket(buf, null);
            NetworkManager.sendToPlayers(JUtils.around(serverWorld, entity.position(), 128), GRAB_S2C, buf);
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

    public static LazyOptional<GrabCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
    public static GrabCapability getCapability(Entity entity) {
        return entity.getCapability(CAPABILITY).orElse(new GrabCapability(entity));
    }
}
