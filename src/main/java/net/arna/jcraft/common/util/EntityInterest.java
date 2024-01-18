package net.arna.jcraft.common.util;

import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityInterest {
    @Getter
    private ItemInterestType type = ItemInterestType.NONE;
    @Getter
    private Vec3d attractionPos;
    @Getter
    private Item attractionItem;
    @Getter
    private BlockPos attractionBlockPos;

    public EntityInterest() {

    }
    public EntityInterest(ItemInterestType type) {
        this();
        this.type = type;
    }

    public enum ItemInterestType {
        NONE,
        BLOCK_ATTRACTION,
        ITEM_ATTRACTION,
        ENTITY_ATTRACTION, //todo: these two
        STAND_USER
    }

    public static EntityInterest blockAttractionInterest(BlockPos attractionBlockPos) {
        EntityInterest interest = new EntityInterest(ItemInterestType.BLOCK_ATTRACTION);
        interest.attractionBlockPos = attractionBlockPos;
        return interest;
    }

    public static EntityInterest itemAttractionInterest(Item attractionItem) {
        EntityInterest interest = new EntityInterest(ItemInterestType.ITEM_ATTRACTION);
        interest.attractionItem = attractionItem;
        return interest;
    }
}