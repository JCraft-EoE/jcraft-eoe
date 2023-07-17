package net.arna.jcraft.client.model.armor;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.DIOArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DIOArmorModel extends AnimatedGeoModel<DIOArmorItem> {
    @Override
    public Identifier getModelResource(DIOArmorItem object) {
        return JCraft.id("geo/diooutfit.geo.json");
    }

    @Override
    public Identifier getTextureResource(DIOArmorItem object) {
        return JCraft.id("textures/item/diooutfit.png");
    }

    @Override
    public Identifier getAnimationResource(DIOArmorItem animatable) {
        return JCraft.id("animations/diooutfit.animation.json");
    }
}
