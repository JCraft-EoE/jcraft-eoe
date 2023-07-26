package net.arna.jcraft.common.util;

import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ItemInterest {
    @Getter
    private ItemInterestType type = ItemInterestType.NONE;
    @Getter
    private Vec3d attractionPos;
    @Getter
    private BlockPos attractionBlockPos;

    public ItemInterest() {

    }
    public ItemInterest(ItemInterestType type) {
        this();
        this.type = type;
    }

    public enum ItemInterestType {
        NONE,
        BLOCK_ATTRACTION,
        REVOLVER_ATTRACTION;
    }

    public static ItemInterest blockAttractionInterest(BlockPos attractionBlockPos) {
        ItemInterest interest = new ItemInterest(ItemInterestType.BLOCK_ATTRACTION);
        interest.attractionBlockPos = attractionBlockPos;
        return interest;
    }

    public static ItemInterest revolverAttractionInterest() {
        return new ItemInterest(ItemInterestType.REVOLVER_ATTRACTION);
    }
}