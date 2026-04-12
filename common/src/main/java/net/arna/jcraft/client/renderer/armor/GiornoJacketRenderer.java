package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class GiornoJacketRenderer extends ArmorRenderer {

    public static final String ID = "giornojacket";

    public GiornoJacketRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), new GiornoJacketBoneContext(), ID);
    }

    protected static class GiornoJacketBoneContext extends AzArmorBoneContext {
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
