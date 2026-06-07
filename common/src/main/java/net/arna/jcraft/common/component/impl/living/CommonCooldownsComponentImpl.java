package net.arna.jcraft.common.component.impl.living;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CommonCooldownsComponentImpl implements CommonCooldownsComponent {
    public final Object2IntMap<CooldownType> cooldowns = new Object2IntRBTreeMap<>();
    protected final Object2IntMap<CooldownType> initialDurations = new Object2IntRBTreeMap<>();
    private final Entity entity;
    public boolean skipSync;

    public CommonCooldownsComponentImpl(final @NonNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public int getCooldown(final CooldownType type) {
        return Math.max(cooldowns.getOrDefault(type, 0), 0);
    }

    @Override
    public int getInitialDuration(final CooldownType type) {
        return initialDurations.getOrDefault(type, 0);
    }

    @Override
    public void setCooldown(final CooldownType type, int duration) {
        if (duration == 0) {
            clear(type);
            return;
        }

        if (type.isOverrideNoCooldowns() || JServerConfig.ENABLE_MOVE_COOLDOWNS.getValue()) {
            duration = (int) (duration * JServerConfig.COOLDOWN_MULTIPLIER.getValue());
            cooldowns.put(type, duration);
            initialDurations.put(type, duration);
            sync(entity);
        }
    }

    @Override
    public void cooldownCancel() {
        if (entity.isSpectator()) {
            return;
        }

        if (entity instanceof Player player && player.isCreative()) {
            // Creative gets boring cooldown cancel.
            clear();
            return;
        }

        if (getCooldown(CooldownType.COOLDOWN_CANCEL) > 0) {
            return;
        }

        if (!JServerConfig.ENABLE_MOVE_COOLDOWNS.getValue()) {
            return;
        }

        skipSync = true;
        for (CooldownType type : CooldownType.values()) {
            if (!type.isNonResettable()) {
                clear(type);
            }
        }
        skipSync = false;

        startCooldown(CooldownType.COOLDOWN_CANCEL);

        final Vec3 pPos = entity.getEyePosition();
        entity.level().playSound(null, entity, JSoundRegistry.COOLDOWN_CANCEL.get(), SoundSource.PLAYERS, 1, 1);
        if (!entity.level().isClientSide) {
            JCraft.createParticle((ServerLevel) entity.level(), pPos.x, pPos.y, pPos.z, JParticleType.COOLDOWN_CANCEL);
        }

        sync(entity);
    }

    @Override
    public void clear(final CooldownType type) {
        cooldowns.put(type, 0);
        initialDurations.put(type, 0);
        sync(entity);
    }

    @Override
    public void clear() {
        skipSync = true;
        for (CooldownType type : CooldownType.values()) {
            clear(type);
        }
        skipSync = false;

        sync(entity);
    }

    public void sync(final Entity entity) {

    }

    public void tick() {
        boolean isClient = entity.level().isClientSide;
        // Decrement all cooldowns.
        boolean shouldSync = false;
        for (Object2IntMap.Entry<CooldownType> entry : cooldowns.object2IntEntrySet()) {
            // Leave at 1 tick on clients. We'll get a sync packet from the server when
            // it actually reaches 0.
            if (isClient && entry.getIntValue() <= 1 || entry.getIntValue() <= 0) {
                continue;
            }

            entry.setValue(entry.getIntValue() - 1);
            if (!isClient && entry.getIntValue() <= 0) {
                shouldSync = true;
            }
        }

        if (shouldSync) {
            sync(entity);
        }
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        readMap(cooldowns, tag.getCompound("Cooldowns"));
        readMap(initialDurations, tag.getCompound("InitialDurations"));
    }

    private static void readMap(final Object2IntMap<CooldownType> map, CompoundTag tag) {
        for (CooldownType type : CooldownType.values()) {
            if (tag.contains(type.name(), Tag.TAG_INT)) {
                map.put(type, tag.getInt(type.name()));
            } else {
                map.removeInt(type);
            }
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        tag.put("Cooldowns", writeMap(this.cooldowns));
        tag.put("InitialDurations", writeMap(this.initialDurations));
    }

    private static CompoundTag writeMap(final Object2IntMap<CooldownType> map) {
        CompoundTag nbt = new CompoundTag();
        map.object2IntEntrySet().forEach(entry -> {
            if (entry.getIntValue() > 0) {
                nbt.putInt(entry.getKey().name(), entry.getIntValue());
            }
        });
        return nbt;
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == entity; // Others don't need to know our cooldowns.
    }
}
