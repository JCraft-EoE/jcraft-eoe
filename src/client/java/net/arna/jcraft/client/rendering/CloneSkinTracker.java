package net.arna.jcraft.client.rendering;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.Pair;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@UtilityClass
public class CloneSkinTracker {
    private static final Map<PlayerCloneEntity, Pair<Identifier, String>> modelCache = new WeakHashMap<>();
    private static final Map<PlayerCloneEntity, PlayerCloneClientPlayerEntity> playerCache = new WeakHashMap<>();
    private static final Set<PlayerCloneEntity> loading = Collections.newSetFromMap(new WeakHashMap<>());

    public static Pair<Identifier, String> getSkinFor(PlayerCloneEntity clone) {
        if (!modelCache.containsKey(clone)) load(clone);
        Pair<Identifier, String> pair = modelCache.get(clone);
        return pair == null ? getDefault(clone) : pair;
    }

    public static PlayerCloneClientPlayerEntity toPlayer(PlayerCloneEntity clone) {
        PlayerCloneClientPlayerEntity clonePlayer = playerCache.computeIfAbsent(clone, PlayerCloneClientPlayerEntity::new);
        clonePlayer.updateData();
        return clonePlayer;
    }

    private static void load(PlayerCloneEntity clone) {
        GameProfile profile = clone.getGameProfile();
        if (profile == null) return;

        synchronized (loading) {
            if (loading.contains(clone)) return;
            loading.add(clone);
        }

        MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, id, texture) -> {
            String model;
            if (type != MinecraftProfileTexture.Type.SKIN || (model = texture.getMetadata("model")) == null) return;

            modelCache.put(clone, Pair.of(id, model));

            synchronized (loading) {
                loading.remove(clone);
            }
        }, true);
    }

    private static Pair<Identifier, String> getDefault(PlayerCloneEntity clone) {
        return Pair.of(DefaultSkinHelper.getTexture(clone.getMasterId()), DefaultSkinHelper.getModel(clone.getMasterId()));
    }
}
