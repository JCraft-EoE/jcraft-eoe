package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.client.model.armor.JotaroArmorModel;
import net.arna.jcraft.common.item.JotaroArmorItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class JotaroArmorRenderer extends GeoArmorRenderer<JotaroArmorItem> {
    public JotaroArmorRenderer() {
        super(new JotaroArmorModel());
        this.headBone = "helmet";
        this.bodyBone = "chestplate";
        this.rightArmBone = "rightArm";
        this.leftArmBone = "leftArm";
        this.rightLegBone = "rightLeg";
        this.leftLegBone = "leftLeg";
        this.rightBootBone = "rightBoot";
        this.leftBootBone = "leftBoot";
    }
}