package net.arna.jcraft.client.renderer.armor;

import net.arna.jcraft.client.model.armor.JArmorModel;
import net.arna.jcraft.common.item.JotaroArmorItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class DIOCapeRenderer extends GeoArmorRenderer<JotaroArmorItem> {
    public DIOCapeRenderer() {
        super(new JArmorModel<>("diocape"));
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