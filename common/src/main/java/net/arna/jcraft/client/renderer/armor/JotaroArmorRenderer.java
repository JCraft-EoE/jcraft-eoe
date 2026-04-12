package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class JotaroArmorRenderer extends ArmorRenderer {

    public static final String ID = "jotaroclothes";

    public JotaroArmorRenderer() {
        super(() -> new ArmorAnimator(ID), new JotaroArmorBoneContext(), ID);
    }

    protected static class JotaroArmorBoneContext extends AzArmorBoneContext {
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
            else if (currentSlot == EquipmentSlot.HEAD) {
                setBoneVisible(this.head, true);
            }
        }
    }
}
