package net.arna.jcraft.forge.capability.impl.living;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.living.CommonHitPropertyComponentImpl;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Set;
import java.util.stream.Collectors;

public class HitPropertyCapability extends CommonHitPropertyComponentImpl implements JCapability {
    public static ResourceLocation HIT_S2C = JCraft.id("hit_s2c");

    public static Capability<HitPropertyCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public HitPropertyCapability(LivingEntity living) {
        super(living);
    }

    @Override
    public void setHitAnimation(HitAnimation hitAnimation, int duration) {
        super.setHitAnimation(hitAnimation, duration);

        if (entity instanceof LivingEntity livingEntity) {
            if (entity.level() instanceof ServerLevel serverWorld) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeVarInt(livingEntity.getId());
                writeSyncPacket(buf, null);
                final Set<ServerPlayer> recipients = serverWorld.players()
                        .stream()
                        .filter(this::shouldSyncWith)
                        .collect(Collectors.toUnmodifiableSet());
                NetworkManager.sendToPlayers(recipients, HIT_S2C, buf);
            }
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

    public static LazyOptional<HitPropertyCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY);
    }
    public static HitPropertyCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new HitPropertyCapability(entity));
    }
}
