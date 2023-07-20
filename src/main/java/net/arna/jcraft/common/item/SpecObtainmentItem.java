package net.arna.jcraft.common.item;

import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ISpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public abstract class SpecObtainmentItem extends Item {
    protected boolean warned = false;
    protected JCraftSpec switchTo;

    public SpecObtainmentItem(Settings settings, JCraftSpec switchTo) {
        super(settings);
        this.switchTo = switchTo;
    }

    private boolean setSpec(PlayerEntity player) {
        if (player == null) return false;
        NbtCompound playerData = ((IEntityDataSaver)player).getPersistentData();
        playerData.putInt("SpecID", switchTo.getId());
        JUtils.assignSpec(player, playerData, (ISpec)player );
        warned = false;
        return true;
    }

    public boolean tryGetSpec(PlayerEntity player) {
        JCraftSpec spec = JUtils.getSpec(player);
        if (spec != null) { // If the player already has a spec
            if (spec.getId() != switchTo.getId()) { // And it isn't the one that will be switched to
                if (!warned) {
                    player.sendMessage(Text.translatable("warning.jcraft.spec.change"));
                    warned = true;
                } else {
                    return setSpec(player);
                }
            }
        } else {
            return setSpec(player);
        }

        return false;
    }
}
