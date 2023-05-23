package net.arna.jcraft.registry;

import net.arna.jcraft.client.renderer.entity.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModEntityRendererRegister {
    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(ModEntityRegister.STARPLATINUM, StarPlatinumRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.KINGCRIMSON, KingCrimsonRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.D4C, D4CRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.PLAYERCLONE, ctx -> new PlayerCloneRenderer(ctx, false));
        EntityRendererRegistry.register(ModEntityRegister.PLAYERCLONE_SLIM, ctx -> new PlayerCloneRenderer(ctx, true));

        EntityRendererRegistry.register(ModEntityRegister.CREAM, CreamRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.KILLERQUEEN, KillerQueenRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.KQBTD, KQBTDRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.SHA, SheerHeartAttackRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.WHITESNAKE, WhitesnakeRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.CMOON, CMoonRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.MIH, MadeInHeavenRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.THEWORLD, TheWorldRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.TWOH, TheWorldOverHeavenRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.SILVERCHARIOT, SilverChariotRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.MAGICIANSRED, MagiciansRedRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.THEFOOL, TheFoolRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.GOLDENEXPERIENCE, GoldenExperienceRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.GETREE, GETreeRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.GESNAKE, GESnakeRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.GER, GERRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.GERSCORPION, GERScorpionRenderer::new);

        EntityRendererRegistry.register(ModEntityRegister.BLOODPROJECTILE, BloodProjectileRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.KNIFE, KnifeRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.ANKH, AnkhRenderer::new);
        EntityRendererRegistry.register(ModEntityRegister.BUBBLE, BubbleRenderer::new);
    }
}
