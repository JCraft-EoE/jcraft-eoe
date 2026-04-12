package net.arna.jcraft.client.renderer.armor;

import mod.azure.azurelib.render.armor.bone.AzArmorBoneContext;
import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class KiraArmorRenderer extends ArmorRenderer {
    public static final String ID = "kiraoutfit";
    public static final ResourceLocation MODEL = JCraft.id(MODEL_STR_TEMPLATE.formatted(ID));
    public static final ResourceLocation TEXTURE = JCraft.id(TEXTURE_STR_TEMPLATE.formatted(ID));

    public KiraArmorRenderer() {
        super(() -> new ArmorAnimator(ID), new KiraArmorBoneContext(), MODEL, TEXTURE);
    }

    public static class KiraArmorBoneContext extends AzArmorBoneContext {
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
