package net.arna.jcraft.client.rendering;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.Pair;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.client.util.PlayerCloneClientPlayerEntity;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class CloneSkinTracker {
    private static final LoadingCache<PlayerCloneEntity, Pair<Identifier, String>> modelCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .weakKeys()
            .build(CacheLoader.from(CloneSkinTracker::load));
    private static final Map<PlayerCloneEntity, PlayerCloneClientPlayerEntity> playerCache = new WeakHashMap<>();

    public static Pair<Identifier, String> getSkinFor(PlayerCloneEntity clone) {
        return modelCache.getUnchecked(clone);
    }

    public static PlayerCloneClientPlayerEntity toPlayer(PlayerCloneEntity clone) {
        PlayerCloneClientPlayerEntity clonePlayer = playerCache.computeIfAbsent(clone, PlayerCloneClientPlayerEntity::new);
        clonePlayer.updateData();
        return clonePlayer;
    }

    private static Pair<Identifier, String> load(PlayerCloneEntity clone) {
        GameProfile profile = clone.getGameProfile();
        MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, id, texture) -> {
            String model;
            if (type != MinecraftProfileTexture.Type.SKIN || (model = texture.getMetadata("model")) == null) return;

            modelCache.put(clone, Pair.of(id, model));
        }, true);

        // Always return default model until the skin is loaded.
        // The above call is not blocking, but will overwrite the model in the cache
        // once it's done loading.
        return Pair.of(DefaultSkinHelper.getTexture(clone.getMasterId()), DefaultSkinHelper.getModel(clone.getMasterId()));
    }
}
