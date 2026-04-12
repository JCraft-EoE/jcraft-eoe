package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders cap
public class RisottoCapRenderer extends ArmorRenderer {

    public static final String ID = "risottocap";

    public RisottoCapRenderer() {
        super(() -> new ArmorAnimator(ID), new RisottoCapBoneContext(), ID);
    }

    public static class RisottoCapBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.HEAD) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.head, true);
            }
        }
    }
}
