package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class JotaroCoatP6Renderer extends ArmorRenderer {

    public static final String ID = "jotarocoatp6";

    public JotaroCoatP6Renderer() {
        super(() -> new FlutteringArmorAnimator(ID), new JotaroCoatP6BoneContext(), ID);
    }

    protected static class JotaroCoatP6BoneContext extends AzArmorBoneContext {
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
