package net.arna.jcraft.mixin_logic;

import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityMixinLogic {

    public static void jcraft$doNotPlayDesummonSoundWhenMovingWorld(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        StandEntity<?, ?> stand = JUtils.getStand(living);
        if (stand == null) {
            return;
        }

        stand.setPlayDesummonSound(false);
    }

}
