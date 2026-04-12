package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class GyroBottomRenderer extends ArmorRenderer {

    public static final String ID = "gyrobottom";

    public GyroBottomRenderer() {
        super(() -> new ArmorAnimator(ID), new GyroBottomBoneContext(), ID);
    }

    protected static class GyroBottomBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.LEGS) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
            }
            else if (currentSlot == EquipmentSlot.FEET) {
                setBoneVisible(this.leftBoot, true);
                setBoneVisible(this.rightBoot, true);
            }
        }
    }
}
