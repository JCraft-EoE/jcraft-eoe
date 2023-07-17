package net.arna.jcraft.client.registry;

import net.arna.jcraft.client.renderer.entity.*;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public interface JEntityRendererRegister {
    static void registerEntityRenderers() {
        EntityRendererRegistry.register(JEntityTypeRegister.STAR_PLATINUM, StarPlatinumRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.SPTW, SPTWRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.KING_CRIMSON, KingCrimsonRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.D4C, D4CRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.PLAYER_ENTITY_CLONE, ctx -> new PlayerCloneRenderer(ctx, false));
        EntityRendererRegistry.register(JEntityTypeRegister.PLAYER_ENTITY_CLONE_SLIM, ctx -> new PlayerCloneRenderer(ctx, true));

        EntityRendererRegistry.register(JEntityTypeRegister.CREAM, CreamRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.KILLER_QUEEN, KillerQueenRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.KILLER_QUEEN_BITES_THE_DUST, KQBTDRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.SHEER_HEART_ATTACK, SheerHeartAttackRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.WHITE_SNAKE, WhiteSnakeRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.C_MOON, CMoonRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.MADE_IN_HEAVEN, MadeInHeavenRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.THE_WORLD, TheWorldRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.SILVER_CHARIOT, SilverChariotRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.MAGICIANS_RED, MagiciansRedRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.THE_FOOL, TheFoolRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.GOLD_EXPERIENCE, GoldenExperienceRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.GE_TREE, GETreeRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.GE_FROG, GEFrogRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.GE_SNAKE, GESnakeRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.GER, GERRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.GER_SCORPION, GERScorpionRenderer::new);

        EntityRendererRegistry.register(JEntityTypeRegister.BLOOD_PROJECTILE, BloodProjectileRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.BLOCK_PROJECTILE, BlockProjectileRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.KNIFE, KnifeRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.ANKH, AnkhRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.BUBBLE, BubbleRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.LIFE_DETECTOR, LifeDetectorRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.SAND_TORNADO, SandTornadoRenderer::new);
        EntityRendererRegistry.register(JEntityTypeRegister.WS_ACID_PROJECTILE, WSAcidRenderer::new);
    }
}
