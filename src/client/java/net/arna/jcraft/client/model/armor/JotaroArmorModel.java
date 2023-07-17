package net.arna.jcraft.client.model.armor;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.JotaroArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class JotaroArmorModel extends AnimatedGeoModel<JotaroArmorItem> {
    @Override
    public Identifier getModelResource(JotaroArmorItem object) {
        return JCraft.id("geo/jotarooutfit.geo.json");
    }

    @Override
    public Identifier getTextureResource(JotaroArmorItem object) {
        return JCraft.id("textures/item/jotarooutfit.png");
    }

    @Override
    public Identifier getAnimationResource(JotaroArmorItem animatable) {
        return JCraft.id("animations/jotarooutfit.animation.json");
    }
}
