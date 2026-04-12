package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

//renders coat
public class KosakuJacketRenderer extends ArmorRenderer{
    public static final String ID = "kosakujacket";
    public static final ResourceLocation MODEL = JCraft.id(MODEL_STR_TEMPLATE.formatted("kirajacket"));
    public static final ResourceLocation TEXTURE = JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID));

    public KosakuJacketRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), MODEL, TEXTURE);
    }
}
