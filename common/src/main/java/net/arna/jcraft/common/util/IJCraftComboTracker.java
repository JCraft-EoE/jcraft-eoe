package net.arna.jcraft.common.util;

import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.minecraft.world.entity.LivingEntity;

public interface IJCraftComboTracker {
    float jcraft$getDamageScaling();

    int jcraft$getHitCount();

    boolean jcraft$addMoveToCombo(LivingEntity attacker, MoveUsage moveUsage);

    boolean jcraft$comboFromAttackerContains(LivingEntity attacker, AbstractMove<?, ?> move);

    void jcraft$increaseHitCount(boolean tsHit);

    void jcraft$resetCombo();
}
