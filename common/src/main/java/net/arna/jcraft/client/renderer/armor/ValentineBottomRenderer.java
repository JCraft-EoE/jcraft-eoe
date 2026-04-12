package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class ValentineBottomRenderer extends ArmorRenderer {

    public static final String ID = "valentinebottom";

    public ValentineBottomRenderer() {
        super(() -> new ArmorAnimator(ID), new ValentineBottomBoneContext(), ID);
    }

    public static class ValentineBottomBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.LEGS) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.rightArm, true);
            }
            else if (currentSlot == EquipmentSlot.FEET) {
                setBoneVisible(this.leftBoot, true);
                setBoneVisible(this.rightBoot, true);
            }
        }
    }
}
