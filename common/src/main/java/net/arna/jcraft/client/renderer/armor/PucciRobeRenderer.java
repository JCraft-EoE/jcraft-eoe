package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class PucciRobeRenderer extends ArmorRenderer {

    public static final String ID = "puccirobe";

    public PucciRobeRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), new PucciRobeBoneContext(), ID);
    }

    public static class PucciRobeBoneContext extends AzArmorBoneContext {
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
