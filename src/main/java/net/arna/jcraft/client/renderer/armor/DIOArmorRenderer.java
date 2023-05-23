package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.client.model.armor.DIOArmorModel;
import net.arna.jcraft.common.item.DIOArmorItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class DIOArmorRenderer extends GeoArmorRenderer<DIOArmorItem> {
    public DIOArmorRenderer() {
        super(new DIOArmorModel());
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