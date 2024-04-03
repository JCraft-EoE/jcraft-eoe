package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.client.model.armor.JArmorModel;
import net.arna.jcraft.common.item.SunProtectionItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class KarsArmorRenderer extends GeoArmorRenderer<SunProtectionItem> {
    public KarsArmorRenderer() {
        super(new JArmorModel<>("karsoutfit"));
        /*
        this.headBone = "helmet";
        this.bodyBone = "chestplate";
        this.rightArmBone = "rightArm";
        this.leftArmBone = "leftArm";
        this.rightLegBone = "rightLeg";
        this.leftLegBone = "leftLeg";
        this.rightBootBone = "rightBoot";
        this.leftBootBone = "leftBoot";

         */
    }
}