package net.arna.jcraft.common.component.impl;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.entity.StandType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class StandComponentImpl implements StandComponent {
    private final Entity entity;
    @Getter
    private StandType type;
    @Getter
    private int skin;

    public StandComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setType(@Nullable StandType type) {
        this.type = type;
        if (type == null) skin = 0;
        sync();
    }

    @Override
    public void setSkin(int skin) {
        if (type == null) return;

        this.skin = MathHelper.clamp(skin, 0, type.getSkinCount());
        sync();
    }

    @Nullable
    @Override
    public StandEntity<?, ?> getStand() {
        return entity.getFirstPassenger() instanceof StandEntity<?, ?> stand ? stand : null;
    }

    private void sync() {
        JComponents.STAND.sync(entity);
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        int rawType = tag.getInt("Type");
        type = rawType == 0 ? null : StandType.fromId(rawType);
        skin = tag.getInt("Skin");
        // Stand is not persistent
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.putInt("Type", type == null ? 0 : type.getId());
        tag.putInt("Skin", skin);
    }
}
