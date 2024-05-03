package net.arna.jcraft.common.component.impl.living;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class CooldownsComponentImpl implements CooldownsComponent {
    private final Object2IntMap<CooldownType> cooldowns = new Object2IntRBTreeMap<>(),
            initialDurations = new Object2IntRBTreeMap<>();
    private final Entity entity;
    private boolean skipSync;

    public CooldownsComponentImpl(@NonNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getCooldown(CooldownType type) {
        return Math.max(cooldowns.getOrDefault(type, 0), 0);
    }

    @Override
    public int getInitialDuration(CooldownType type) {
        return initialDurations.getOrDefault(type, 0);
    }

    @Override
    public void setCooldown(CooldownType type, int duration) {
        if (duration == 0) {
            clear(type);
            return;
        }

        if (type.isOverrideNoCooldowns() || JServerConfig.ENABLE_MOVE_COOLDOWNS.getValue()) {
            duration *= JServerConfig.COOLDOWN_MULTIPLIER.getValue();
            cooldowns.put(type, duration);
            initialDurations.put(type, duration);
            sync();
        }
    }

    @Override
    public void cooldownCancel() {
        if (entity.isSpectator()) return;

        if (entity instanceof PlayerEntity player && player.isCreative()) {
            // Creative gets boring cooldown cancel.
            clear();
            return;
        }

        if (getCooldown(CooldownType.COOLDOWN_CANCEL) > 0) return;

        if (!JServerConfig.ENABLE_MOVE_COOLDOWNS.getValue())
            return;

        skipSync = true;
        for (CooldownType type : CooldownType.values())
            if (!type.isNonResettable())
                clear(type);
        skipSync = false;

        startCooldown(CooldownType.COOLDOWN_CANCEL);

        Vec3d pPos = entity.getEyePos();
        entity.getWorld().playSoundFromEntity(null, entity, JSoundRegistry.COOLDOWN_CANCEL, SoundCategory.PLAYERS, 1, 1);
        if (!entity.getWorld().isClient) JCraft.createParticle((ServerWorld) entity.getWorld(), pPos.x, pPos.y, pPos.z, JParticleType.COOLDOWN_CANCEL);

        sync();
    }

    @Override
    public void clear(CooldownType type) {
        cooldowns.put(type, 0);
        initialDurations.put(type, 0);
        sync();
    }

    @Override
    public void clear() {
        skipSync = true;
        for (CooldownType type : CooldownType.values())
            clear(type);
        skipSync = false;

        sync();
    }

    private void sync() {
        if (skipSync) return; // To avoid packet spam.
        JComponents.COOLDOWNS.sync(entity);
    }

    @Override
    public void tick() {
        boolean isClient = entity.getWorld().isClient;

        // Decrement all cooldowns.
        boolean shouldSync = false;
        for (Object2IntMap.Entry<CooldownType> entry : cooldowns.object2IntEntrySet()) {
            // Leave at 1 tick on clients. We'll get a sync packet from the server when
            // it actually reaches 0.
            if (isClient && entry.getIntValue() <= 1 || entry.getIntValue() <= 0) continue;

            entry.setValue(entry.getIntValue() - 1);
            if (!isClient && entry.getIntValue() <= 0) shouldSync = true;
        }

        if (shouldSync) sync();
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        readMap(cooldowns, tag.getCompound("Cooldowns"));
        readMap(initialDurations, tag.getCompound("InitialDurations"));
    }

    private static void readMap(Object2IntMap<CooldownType> map, NbtCompound tag) {
        for (CooldownType type : CooldownType.values())
            if (tag.contains(type.name(), NbtElement.INT_TYPE))
                map.put(type, tag.getInt(type.name()));
            else map.removeInt(type);
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.put("Cooldowns", writeMap(this.cooldowns));
        tag.put("InitialDurations", writeMap(this.initialDurations));
    }

    private static NbtCompound writeMap(Object2IntMap<CooldownType> map) {
        NbtCompound nbt = new NbtCompound();
        map.object2IntEntrySet().forEach(entry -> {
            if (entry.getIntValue() > 0) nbt.putInt(entry.getKey().name(), entry.getIntValue());
        });
        return nbt;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == entity; // Others don't need to know our cooldowns.
    }
}
