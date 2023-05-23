package net.arna.jcraft.registry;

import net.arna.jcraft.client.renderer.armor.DIOArmorRenderer;
import net.arna.jcraft.client.renderer.armor.JotaroArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public interface JArmorRendererRegister {

    static void registerArmorRenderers() {
        GeoArmorRenderer.registerArmorRenderer(new DIOArmorRenderer(), JObjectRegistry.DIOHEADBAND,
                JObjectRegistry.DIOJACKET, JObjectRegistry.DIOPANTS, JObjectRegistry.DIOBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new JotaroArmorRenderer(), JObjectRegistry.JOTAROCAP,
                JObjectRegistry.JOTAROJACKET, JObjectRegistry.JOTAROPANTS, JObjectRegistry.JOTAROBOOTS);
    }
}
