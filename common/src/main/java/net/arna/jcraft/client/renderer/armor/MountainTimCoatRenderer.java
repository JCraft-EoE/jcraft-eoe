package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class MountainTimCoatRenderer extends ArmorRenderer {

    public static final String ID = "mountain_tim_coat";

    public MountainTimCoatRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), new MountainTimCoatBoneContext(), ID);
    }

    protected static class MountainTimCoatBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.CHEST) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.rightArm, true);
                setBoneVisible(this.head, true);
            }
        }
    }
}
