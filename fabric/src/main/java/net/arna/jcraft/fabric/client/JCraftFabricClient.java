package net.arna.jcraft.fabric.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.events.JClientEvents;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.registry.JEntityModelLayerRegistry;
import net.arna.jcraft.client.registry.JEntityRendererRegister;
import net.arna.jcraft.client.registry.JItemPropertiesRegistry;
import net.arna.jcraft.client.renderer.block.CoffinTileRenderer;
import net.arna.jcraft.client.renderer.effects.*;
import net.arna.jcraft.api.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public final class JCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        JCraftClient.init();
        JEntityModelLayerRegistry.init(modelLayerDefinition -> EntityModelLayerRegistry.register(modelLayerDefinition.getKey(), modelLayerDefinition.getValue()));
        JEntityRendererRegister.registerEntityRenderers(JEntityRendererRegister.RendererData::registerFabric);
        BlockEntityRenderers.register(JBlockEntityTypeRegistry.COFFIN_TILE.get(), context -> new CoffinTileRenderer());

        JCraftClient.registerParticleSpriteSets();

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            JClientEvents.afterTranslucent(context.matrixStack(), context.camera().getPosition(), context.worldRenderer());
        });

        WorldRenderEvents.LAST.register(context -> {
            JClientEvents.onLast(context.matrixStack(), context.camera().getPosition());
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            AttackHitboxEffectRenderer.render(context.matrixStack(), context.camera().getPosition(), context.worldRenderer(), context.consumers());
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            ShockwaveEffectRenderer.render(context.matrixStack(), context.camera().getPosition(), context.world(), context.consumers());
        });

        WorldRenderEvents.START.register(context -> {
            TimeAccelerationEffectRenderer.render(context.world());
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            SplatterEffectRenderer.render(context.matrixStack(), context.camera().getPosition(), context.world(), context.tickDelta());
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            TimeErasePredictionEffectRenderer.render(context.matrixStack(), context.camera().getPosition(), context.world(), context.tickDelta(), context.consumers());
        });

        if (JItemRegistry.DEBUG_WAND != null) {
            ResourceLocation itemId = JItemRegistry.ITEMS.get(JItemRegistry.DEBUG_WAND);
            BigItemRenderer itemRenderer = new BigItemRenderer(itemId);


            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(itemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(JItemRegistry.DEBUG_WAND.get(), itemRenderer);

            ModelLoadingPlugin.register(ctx -> ctx.addModels(
                    new ModelResourceLocation(new ResourceLocation(itemId + "_gui"), "inventory"),
                    new ModelResourceLocation(new ResourceLocation(itemId + "_handheld"), "inventory")));
        }

        JItemPropertiesRegistry.registerItemProperties();
        JCraftClient.registerKeyBindings(null);

        // Run when the MinecraftClient instance is fully initialized.
        Minecraft.getInstance().tell(EpitaphOverlay::preload);
    }
}
