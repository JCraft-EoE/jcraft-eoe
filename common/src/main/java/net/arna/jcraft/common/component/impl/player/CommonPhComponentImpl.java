package net.arna.jcraft.common.component.impl.player;

import lombok.NonNull;
import net.arna.jcraft.api.component.player.CommonPhComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class CommonPhComponentImpl implements CommonPhComponent {
    private final Player player;
    private int level = 0;

    protected CommonPhComponentImpl(final Player player) {
        this.player = player;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void increaseLevel() {
        level++;
    }

    @Override
    public void resetLevel() {
        level = 0;
    }

    public void sync(final Entity entity) {
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        level = tag.getInt("level");
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        tag.putInt("level", level);
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == this.player; // Only our player needs to know, I believe.
    }
}
