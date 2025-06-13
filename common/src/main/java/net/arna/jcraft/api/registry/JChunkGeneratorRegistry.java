package net.arna.jcraft.api.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.worldgen.TutorialChunkGenerator;

public interface JChunkGeneratorRegistry {

    static void register() {
        JCraft.CHUNK_GENERATORS.register(JCraft.id("tutorial"), () -> TutorialChunkGenerator.CODEC);
    }

}
