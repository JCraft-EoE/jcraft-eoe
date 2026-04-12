package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

public class KosakuArmorRenderer extends ArmorRenderer {

    public static final String ID = "kosakuoutfit";
    public static final ResourceLocation TEXTURE = JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID));

    public KosakuArmorRenderer() {
        super(() -> new ArmorAnimator(ID), new KiraArmorRenderer.KiraArmorBoneContext(), KiraArmorRenderer.MODEL, TEXTURE);
    }
}
