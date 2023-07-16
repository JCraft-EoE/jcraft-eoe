package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;

public class CinderellaMaskItem extends Item {
    public CinderellaMaskItem() {
        super(new Settings()
                .group(JCraft.JCRAFT_GROUP)
                .rarity(Rarity.RARE));
    }
}
