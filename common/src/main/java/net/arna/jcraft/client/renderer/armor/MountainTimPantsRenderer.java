package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class MountainTimPantsRenderer extends ArmorRenderer {

    public static final String ID = "mountain_tim_clothes";

    public MountainTimPantsRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), new MountainTimPantsBoneContext(), ID);
    }

    protected static class MountainTimPantsBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.LEGS) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.rightArm, true);
            }
        }
    }
}
