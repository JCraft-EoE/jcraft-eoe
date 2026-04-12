package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

//renders coat
public class FinalKiraJacketRenderer extends ArmorRenderer {
    public static final String ID = "finalkirajacket";
    public static final ResourceLocation TEXTURE = JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID));

    public FinalKiraJacketRenderer() {
        super(() -> new ArmorRenderer.FlutteringArmorAnimator(ID), KosakuJacketRenderer.MODEL, TEXTURE);
    }
}
