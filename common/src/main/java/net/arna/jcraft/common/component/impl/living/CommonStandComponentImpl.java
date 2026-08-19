package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommonStandComponentImpl implements CommonStandComponent {
    private final Entity entity;
    private StandEntity<?, ?> stand;
    private StandType type;
    @Getter
    private int skin;
    @Getter
    private boolean tagged;
    private final Map<StandType, List<Integer>> skinsMap = new HashMap<>();

    public CommonStandComponentImpl(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setTypeAndSkin(final @Nullable StandType type, final int skin, final boolean loading) {
        // Exclusive stand check
        if (!entity.level().isClientSide && entity instanceof Player &&
                !JCraft.getExclusiveStandsData().switchStand(this.type, type)) {
            return;
        }

        if (!StandTypeUtil.isNone(type) && entity instanceof ServerPlayer player) {
            if (!loading) {
                JUtils.maySendStandAboutInfo(player);
            }
            JAdvancementTriggerRegistry.OBTAINED_STAND.trigger(player, type);
        }
        this.type = type;
        this.skin = skin;
        addSkinFor(this.type, this.skin); // TODO add advancement trigger here
        sync(entity);
    }

    @Override
    public void setSkin(final int skin) {
        if (type == null) {
            return;
        }

        this.skin = Mth.clamp(skin, 0, type.getData().getInfo().getSkinCount() - 1);
        addSkinFor(type, this.skin); // TODO add advancement trigger here
        sync(entity);
    }

    @Override
    public void setStand(final @Nullable StandEntity<?, ?> stand) {
        // if (this.stand != null) this.stand.setUser(null);
        this.stand = stand;
        sync(entity);
    }

    @Override
    public @NonNull List<Integer> getSkinsFor(final StandType type) {
        if (!skinsMap.containsKey(type)) {
            return List.of();
        }
        return Collections.unmodifiableList(skinsMap.get(type));
    }

    @Override
    public boolean addSkinFor(final StandType type, final int skin) {
        skinsMap.putIfAbsent(type, new LinkedList<>());
        return skinsMap.get(type).add(skin);
    }

    @Nullable
    @Override
    public StandType getType() {
        if (type == null && stand != null) {
            // this.type = stand.getStandType();
            JCraft.LOGGER.warn("StandType of {} is null despite non-null stand {}", stand.getUser(), stand);
        }
        return this.type;
    }

    @Nullable
    @Override
    public StandEntity<?, ?> getStand() {
        if (stand != null && !stand.isAlive()) {
            setStand(null);
        }
        // Checks if the stand user has a passenger, and updates the stand if the passenger and stand do not match
        if (entity.getFirstPassenger() instanceof StandEntity<?, ?> passenger && stand != passenger) {
            setStand(passenger);
        }
        // Otherwise, returns the stored stand value
        return stand;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
        sync(entity);
    }

    public void sync(Entity entity) {
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        type = StandTypeUtil.readFromNBT(tag, "Type");
        skin = tag.getInt("Skin");
        tagged = tag.getBoolean("Tagged");
        final CompoundTag skinsMapTag = tag.getCompound("SkinsMap");
        skinsMap.clear();
        for (final String typeKeyStr : skinsMapTag.getAllKeys()) {
            final StandType typeKey = JRegistries.STAND_TYPE_REGISTRY.get(new ResourceLocation(typeKeyStr));
            if (typeKey == null) {
                JCraft.LOGGER.warn("Skin for unknown stand {} found, will be ignored!", typeKeyStr);
                continue;
            }
            List<Integer> skinList = skinsMap.computeIfAbsent(typeKey, key -> new LinkedList<>());
            final int[] skins = skinsMapTag.getIntArray(typeKeyStr);
            for (final int skin : skins) {
                if (!skinList.contains(skin)) {
                    skinList.add(skin);
                }
            }
        }
        // always ensure the current skin of the current stand
        if (!StandTypeUtil.isNone(type)) {
            addSkinFor(type, skin);
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        tag.putString("Type", type == null ? "" : type.getId().toString());
        tag.putInt("Skin", skin);
        tag.putBoolean("Tagged", tagged);
        final CompoundTag skinsMapTag = new CompoundTag();
        for (final var entry : skinsMap.entrySet()) {
            if (entry.getKey() != null) {
                skinsMapTag.putIntArray(entry.getKey().getId().toString(), entry.getValue());
            }
        }
        tag.put("SkinsMap", skinsMapTag);
    }

    /**
     * Makes a certain entity be considered the component holders stand.
     */
    public void applySyncPacket(final FriendlyByteBuf buf) {
        Entity entity = buf.readBoolean() ? this.entity.level().getEntity(buf.readVarInt()) : null;
        if (entity == null || entity instanceof StandEntity<?, ?>) {
            stand = (StandEntity<?, ?>) entity;
        }
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        buf.writeBoolean(stand != null);
        if (stand != null) {
            buf.writeVarInt(stand.getId());
        }
    }
}
