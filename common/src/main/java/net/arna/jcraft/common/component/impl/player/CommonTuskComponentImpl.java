package net.arna.jcraft.common.component.impl.player;

import lombok.NonNull;
import net.arna.jcraft.api.component.player.CommonTuskComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CommonTuskComponentImpl implements CommonTuskComponent {
    private final Player player;
    private int highestUnlockedAct = 1; // Start with Act 1 unlocked

    public CommonTuskComponentImpl(final Player player) {
        this.player = player;
    }

    @Override
    public int getHighestUnlockedAct() {
        return highestUnlockedAct;
    }

    @Override
    public void unlockAct(int act) {
        if (act < 1 || act > 4) {
            throw new IllegalArgumentException("Act must be between 1 and 4, got: " + act);
        }
        // Can only increase, never decrease
        if (act > highestUnlockedAct) {
            highestUnlockedAct = act;
        }
    }

    @Override
    public boolean hasUnlockedAct(int act) {
        if (act < 1 || act > 4) {
            return false;
        }
        return act <= highestUnlockedAct;
    }

    @Override
    public void resetProgression() {
        highestUnlockedAct = 1;
    }

    public void sync(final Entity entity) {
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        highestUnlockedAct = tag.getInt("highestUnlockedAct");

        // Safety check: ensure valid range
        if (highestUnlockedAct < 1) {
            highestUnlockedAct = 1;
        } else if (highestUnlockedAct > 4) {
            highestUnlockedAct = 4;
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        tag.putInt("highestUnlockedAct", highestUnlockedAct);
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == this.player; // Only our player needs to know
    }
}