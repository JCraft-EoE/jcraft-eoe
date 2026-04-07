package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class ValentineTopRenderer extends ArmorRenderer {

    public static final String ID = "valentinecoat";

    public ValentineTopRenderer() {
        super(() -> new ArmorAnimator(ID), new ValentineTopBoneContext(), ID);
    }

    public static class ValentineTopBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.CHEST) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.rightArm, true);
            }
        }
    }
}
