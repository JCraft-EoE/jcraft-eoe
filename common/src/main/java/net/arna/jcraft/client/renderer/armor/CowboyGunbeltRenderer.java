package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

//renders coat
public class CowboyGunbeltRenderer extends ArmorRenderer {

    public static final String ID = "cowboy_outfit";

    public CowboyGunbeltRenderer() {
        super(() -> new ArmorAnimator(ID), new CowboyGunbeltBoneContext(), ID);
    }

    protected static class CowboyGunbeltBoneContext extends AzArmorBoneContext {
        public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.LEGS) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.leftLeg, true);
                setBoneVisible(this.rightLeg, true);
            }
        }
    }
}
