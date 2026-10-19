package net.arna.jcraft.common.component.impl.world;

import lombok.NonNull;
import net.arna.jcraft.api.component.world.CommonTexasHoldEmComponent;
import net.arna.jcraft.common.minigame.card.texasholdem.TexasHoldEm;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public abstract class CommonTexasHoldEmComponentImpl implements CommonTexasHoldEmComponent {

    protected final Level world;
    protected Collection<TexasHoldEm> games = new HashSet<>();

    protected CommonTexasHoldEmComponentImpl(final Level world) {
        this.world = Objects.requireNonNull(world);
    }

    public void readFromNbt(final CompoundTag tag) {
        for (final Tag gameTag : tag.getList("texas_hold_em", Tag.TAG_COMPOUND)) {
            final TexasHoldEm game = new TexasHoldEm();
            game.readFromNbt((CompoundTag)gameTag);
            addGame(game);
        }
    }

    public void writeToNbt(final CompoundTag tag) {
        final ListTag gamesTag = new ListTag();
        for (final TexasHoldEm game : games) {
            final CompoundTag gameTag = new CompoundTag();
            game.writeToNbt(gameTag);
            gamesTag.add(gameTag);
        }
        tag.put("texas_hold_em", gamesTag);
    }

    @Override
    public Collection<TexasHoldEm> getGames() {
        return games;
    }

    @Override
    public boolean addGame(final @NonNull TexasHoldEm game) {
        if (games.contains(game)) {
            return false;
        }
        // you can only be in one game at a time
        for (final TexasHoldEm otherGame : games) {
            for (final String entityUuid : otherGame.getPlayersUuid()) {
                if (game.getPlayersUuid().contains(entityUuid)) {
                    return false;
                }
            }
        }
        return games.add(game);
    }
}
