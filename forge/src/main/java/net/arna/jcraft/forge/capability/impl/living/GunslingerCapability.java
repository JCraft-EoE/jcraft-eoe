package net.arna.jcraft.forge.capability.impl.living;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.living.CommonGunslingerComponentImpl;
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

public class GunslingerCapability extends CommonGunslingerComponentImpl implements JCapability {

    public static ResourceLocation GUNSLINGER_S2C = JCraft.id("gsl_s2c");
    public static Capability<GunslingerCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public GunslingerCapability(LivingEntity living) {
        super(living);
    }

    @Override
    public void sync(Entity entity) {
        super.sync(entity);
        if (entity instanceof ServerPlayer player) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            writeSyncPacket(buf, player);
            NetworkManager.sendToPlayer(player, GUNSLINGER_S2C, buf);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        writeToNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readFromNbt(tag);
    }

    public static LazyOptional<GunslingerCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }

    public static GunslingerCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new GunslingerCapability(entity));
    }

}
