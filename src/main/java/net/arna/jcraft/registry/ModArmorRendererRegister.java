package net.arna.jcraft.registry;

import net.arna.jcraft.client.renderer.armor.DIOArmorRenderer;
import net.arna.jcraft.client.renderer.armor.JotaroArmorRenderer;
import net.arna.jcraft.item.ModItemRegister;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class ModArmorRendererRegister {
    public static void registerArmorRenderers() {
        GeoArmorRenderer.registerArmorRenderer(new DIOArmorRenderer(), ModItemRegister.DIOHEADBAND,
                ModItemRegister.DIOJACKET, ModItemRegister.DIOPANTS, ModItemRegister.DIOBOOTS);
        GeoArmorRenderer.registerArmorRenderer(new JotaroArmorRenderer(), ModItemRegister.JOTAROCAP,
                ModItemRegister.JOTAROJACKET, ModItemRegister.JOTAROPANTS, ModItemRegister.JOTAROBOOTS);
    }
}
