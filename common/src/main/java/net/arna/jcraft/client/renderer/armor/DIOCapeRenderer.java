package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.minecraft.world.entity.EquipmentSlot;

public class DIOCapeRenderer extends ArmorRenderer {

    public static final String ID = "diocape";

    public DIOCapeRenderer() {
        super(() -> new FlutteringArmorAnimator(ID), new DIOCapeBoneContext(), ID);
    }

    protected static class DIOCapeBoneContext extends AzArmorBoneContext {
        @Override
        public void applyBoneVisibilityBySlot(final EquipmentSlot currentSlot) {
            setAllVisible(false);

            if (currentSlot == EquipmentSlot.CHEST) {
                setBoneVisible(this.body, true);
                setBoneVisible(this.head, true);
                setBoneVisible(this.leftArm, true);
                setBoneVisible(this.rightArm, true);
            }
        }
    }

}