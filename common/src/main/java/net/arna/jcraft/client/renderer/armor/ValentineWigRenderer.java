package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class ValentineWigRenderer extends ArmorRenderer {

    public static final String ID = "valentine_wig";

    public ValentineWigRenderer() {
        super(() -> new ArmorAnimator(ID), new ValentineWigBoneContext(), ID);
    }

    public static class ValentineWigBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.HEAD) {
                setBoneVisible(this.head, true);
                setBoneVisible(this.body, true);
            }
        }
    }
}
