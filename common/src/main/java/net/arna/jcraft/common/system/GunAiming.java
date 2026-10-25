package net.arna.jcraft.common.system;

import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GunAiming {
    public static final float SPREAD_MULTIPLIER = 0.25f;

    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("5e9f6a3c-2f5d-4e8b-9c3a-7d1b4f2a6c81");
    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(
            SPEED_MODIFIER_ID, "jcraft_aiming", -0.35, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final Set<UUID> AIMING = ConcurrentHashMap.newKeySet();

    private GunAiming() {}

    public static void set(ServerPlayer player, boolean aiming) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (aiming) {
            AIMING.add(player.getUUID());
            if (speed != null && speed.getModifier(SPEED_MODIFIER_ID) == null) {
                speed.addTransientModifier(SPEED_MODIFIER);
            }
        } else {
            AIMING.remove(player.getUUID());
            if (speed != null) {
                speed.removeModifier(SPEED_MODIFIER_ID);
            }
        }
    }

    public static boolean isAiming(LivingEntity user) {
        return AIMING.contains(user.getUUID());
    }

    public static boolean isAimableGun(ItemStack stack) {
        return stack.getItem() instanceof Peacemaker;
    }
}
