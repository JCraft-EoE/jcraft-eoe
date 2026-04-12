package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class JotaroCoatP4Renderer extends ArmorRenderer {

    public static final String ID = "jotarocoatp4";

    public JotaroCoatP4Renderer() {
        super(() -> new FlutteringArmorAnimator(ID), new JotaroCoatP4BoneContext(), ID);
    }

    protected static class JotaroCoatP4BoneContext extends AzArmorBoneContext {
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
