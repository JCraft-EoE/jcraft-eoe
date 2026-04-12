package net.arna.jcraft.forge.capability.impl.world;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.world.CommonShockwaveHandlerComponentImpl;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public class ShockwaveHandlerCapability extends CommonShockwaveHandlerComponentImpl implements JCapability {

    public static ResourceLocation SHOCK_S2C = JCraft.id("shock_s2c");
    // public static ResourceLocation SHOCK_C2S = JCraft.id("shock_c2s");

    public static Capability<ShockwaveHandlerCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public ShockwaveHandlerCapability(Level world) {
        super(world);
    }

    // NBT serialization is kind of pointless for very short-term, visual effects?
    @Override
    public CompoundTag serializeNBT() {
        // super.writeToNbt(tag);
        return new CompoundTag();
    }
    @Override
    public void deserializeNBT(CompoundTag tag) {
        // super.readFromNbt(tag);
    }

    @Override
    public void sync(final Shockwave shockwave) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        writeSyncPacket(buf, shockwave);
        NetworkManager.sendToPlayers(JUtils.around((ServerLevel)world, shockwave.getBlockPos().getCenter(), 128), SHOCK_S2C, buf);
    }

    public static LazyOptional<ShockwaveHandlerCapability> getCapabilityOptional(Level world) {
        return world.getCapability(CAPABILITY);
    }
    public static ShockwaveHandlerCapability getCapability(Level world) {
        return world.getCapability(CAPABILITY).orElse(new ShockwaveHandlerCapability(world));
    }
}
