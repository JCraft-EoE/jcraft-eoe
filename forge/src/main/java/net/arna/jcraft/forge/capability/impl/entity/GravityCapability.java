package net.arna.jcraft.forge.capability.impl.entity;

import dev.architectury.networking.NetworkManager;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.entity.CommonGravityComponentImpl;
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

public class GravityCapability extends CommonGravityComponentImpl implements JCapability {
    public static ResourceLocation G_S2C = JCraft.id("g_s2c");
    public static ResourceLocation G_C2S = JCraft.id("g_c2s");

    public static Capability<GravityCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public GravityCapability(Entity entity) {
        super(entity);
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

    private static void syncEntityCapability(Entity entity) {
        if (entity instanceof LivingEntity living) {
            JNetworkingForge.sendPackets(living, G_S2C, G_C2S, getCapability(living));
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

    public static Optional<GravityCapability> getCapabilityOptional(Entity user) {
        return user.getCapability(CAPABILITY).resolve();
    }
    public static GravityCapability getCapability(Entity user) {
        return user.getCapability(CAPABILITY).orElse(new GravityCapability(user));
    }

    public static void initServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, G_C2S, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (context.getPlayer().level().getEntity(id) instanceof Player player) {
                GravityCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }
        });
    }
}
