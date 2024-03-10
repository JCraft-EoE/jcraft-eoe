package net.arna.jcraft.client.registry;

import net.arna.jcraft.client.renderer.armor.*;
import net.arna.jcraft.registry.JObjectRegistry;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public interface JArmorRendererRegistry {

    static void registerArmorRenderers() {
        GeoArmorRenderer.registerArmorRenderer(new DIOArmorRenderer(), JObjectRegistry.DIOHEADBAND,
                JObjectRegistry.DIOJACKET, JObjectRegistry.DIOPANTS, JObjectRegistry.DIOBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new JotaroArmorRenderer(), JObjectRegistry.JOTAROCAP,
                JObjectRegistry.JOTAROJACKET, JObjectRegistry.JOTAROPANTS, JObjectRegistry.JOTAROBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new KarsArmorRenderer(), JObjectRegistry.KARSHEADWRAP,
                null, null, null);
        GeoArmorRenderer.registerArmorRenderer(new StoneMaskRenderer(), JObjectRegistry.STONE_MASK,
                null, null, null);
        GeoArmorRenderer.registerArmorRenderer(new RedHatRenderer(), JObjectRegistry.RED_HAT,
                null, null, null);
    }
}
