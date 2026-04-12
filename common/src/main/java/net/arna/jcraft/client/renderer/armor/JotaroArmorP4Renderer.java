package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class JotaroArmorP4Renderer extends ArmorRenderer {

    public static final String ID = "jotaroclothesp4";

    public JotaroArmorP4Renderer() {
        super(() -> new ArmorAnimator(ID), new JotaroArmorP4BoneContext(), ID);
    }

    protected static class JotaroArmorP4BoneContext extends AzArmorBoneContext {
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
            else if (currentSlot == EquipmentSlot.HEAD) {
                setBoneVisible(this.head, true);
            }
        }
    }
}
