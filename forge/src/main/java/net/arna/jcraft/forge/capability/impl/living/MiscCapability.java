package net.arna.jcraft.forge.capability.impl.living;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.living.CommonMiscComponentImpl;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

public class MiscCapability extends CommonMiscComponentImpl implements JCapability {

    public static ResourceLocation MISC_S2C = JCraft.id("misc_s2c");
    public static ResourceLocation MISC_C2S = JCraft.id("misc_c2s");

    public static Capability<MiscCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public MiscCapability(LivingEntity living) {
        super(living);
    }

    @Override
    public void sync(Entity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(entity.getId());
            writeSyncPacket(buf, null);
            NetworkManager.sendToPlayers(JUtils.around(serverLevel, entity.position(), 6400), MISC_S2C, buf);
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

    public static LazyOptional<MiscCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }

    public static MiscCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new MiscCapability(entity));
    }
}
