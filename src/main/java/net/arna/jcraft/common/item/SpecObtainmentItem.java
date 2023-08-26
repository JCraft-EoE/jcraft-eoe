package net.arna.jcraft.common.item;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

public abstract class SpecObtainmentItem extends Item {
    protected boolean warned = false;
    protected final SpecType switchTo;

    public SpecObtainmentItem(Settings settings, SpecType switchTo) {
        super(settings);
        this.switchTo = switchTo;
    }

    private boolean setSpec(PlayerEntity player) {
        if (player == null) return false;

        JComponents.getSpecData(player).setType(switchTo);
        warned = false;
        return true;
    }

    protected boolean tryGetSpec(PlayerEntity player) {
        JSpec<?, ?> spec = JUtils.getSpec(player);
        if (spec != null) { // If the player already has a spec
            if (spec.getType().getId() != switchTo.getId()) { // And it isn't the one that will be switched to
                if (!warned) {
                    player.sendMessage(Text.translatable("warning.jcraft.spec.change"));
                    warned = true;
                } else return setSpec(player);
            }
        } else return setSpec(player);

        return false;
    }
}
