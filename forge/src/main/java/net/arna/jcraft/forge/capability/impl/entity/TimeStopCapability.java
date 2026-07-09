package net.arna.jcraft.forge.capability.impl.entity;

import dev.architectury.networking.NetworkManager;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.entity.CommonTimeStopComponentImpl;
import net.arna.jcraft.forge.JNetworkingForge;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Optional;

public class TimeStopCapability extends CommonTimeStopComponentImpl implements JCapability {

    public static ResourceLocation TIME_S2C = JCraft.id("time_s2c");
    public static ResourceLocation TIME_C2S = JCraft.id("time_c2s");

    public static Capability<TimeStopCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public TimeStopCapability(Entity entity) {
        super(entity);
    }

    @Override
    public void sync(Entity entity) {
        if (entity != null) {
            TimeStopCapability.syncEntityCapability(entity);
        }
    }

    private static void syncEntityCapability(Entity entity) {
        if (entity != null) {
            JNetworkingForge.sendPackets(entity, TIME_S2C, TIME_C2S, getCapability(entity));
        }
    }

    public static void syncEntityCapability(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity livingEntity) {
            if (livingEntity.level() instanceof ServerLevel) {
                syncEntityCapability(livingEntity);
            }
        }
        if (event.getEntity() instanceof Player) {
            if (event.getEntity().level() instanceof ServerLevel) {
                syncEntityCapability(event.getEntity());
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

    public static Optional<TimeStopCapability> getCapabilityOptional(Entity entity) {
        return entity.getCapability(CAPABILITY).resolve();
    }

    public static TimeStopCapability getCapability(Entity entity) {
        return entity.getCapability(CAPABILITY).orElse(new TimeStopCapability(entity));
    }

    public static void initServer(){
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TIME_C2S, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            Entity entity = context.getPlayer().level().getEntity(id);
            if (entity != null) {
                TimeStopCapability.getCapabilityOptional(entity).ifPresent(c -> c.deserializeNBT(nbt));
            }
        });
    }
}
