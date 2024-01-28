package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.events.JServerEvents;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.EntityInterest;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;

import static net.arna.jcraft.common.util.EntityInterest.blockAttractionInterest;
import static net.arna.jcraft.common.util.EntityInterest.itemAttractionInterest;

public interface JEventsRegistry {
    static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {
                    Entity attacker = source.getAttacker();
                    if (!(attacker instanceof LivingEntity living)) return true;

                    // Only apply stun nerfs if hit with a weapon or a projectile
                    boolean hasWeapon = source.isProjectile();
                    if (!hasWeapon) {
                        hasWeapon = !living.getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty();
                    }

                    if (hasWeapon) {
                        StatusEffectInstance stun = entity.getStatusEffect(JStatusRegistry.DAZED);
                        if (stun != null) {
                            int duration = stun.getDuration() / 3;

                            entity.removeStatusEffect(JStatusRegistry.DAZED);
                            StandEntity.stun(entity, duration, 3);

                            Vec3i upVec = GravityChangerAPI.getGravityDirection(entity).getVector();

                            Vec3d knockback = entity.getPos().subtract(attacker.getPos()).normalize()
                                            .add(-upVec.getX() / 3.0, -upVec.getY() / 3.0, -upVec.getZ() / 3.0);

                            GravityChangerAPI.setWorldVelocity(entity, knockback);

                            entity.velocityModified = true;
                        }
                    }
                    return true;
                }
        );

        ServerEntityEvents.ENTITY_LOAD.register(JServerEvents::entityLoad);

        ServerLivingEntityEvents.AFTER_DEATH.register((living, source) -> {
            if (living instanceof ServerPlayerEntity && source.getAttacker() instanceof LivingEntity killer)
                JComponents.getCooldowns(killer).clear(CooldownType.COMBO_BREAKER);
        });

        ServerTickEvents.END_SERVER_TICK.register(JServerEvents::serverTick);

        // Disable item/block usage while stunned
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!JUtils.canAct(player))
                return TypedActionResult.fail(stack);
            return TypedActionResult.pass(stack);
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!JUtils.canAct(player))
                return ActionResult.FAIL;

            // Remote players do stuff with their stand, not themselves
            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand != null && stand.isRemote())
                return ActionResult.FAIL;

            return ActionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!JUtils.canAct(player))
                return ActionResult.FAIL;

            // Remote players do stuff with their stand, not themselves
            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand != null && stand.isRemote())
                return ActionResult.FAIL;

            return ActionResult.PASS;
        });


        // Send initial values of server config options to the player.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ConfigUpdatePacket.sendOptionsToClient(handler.getPlayer(), ConfigOption.getImmutableOptions().values()));

        ServerLifecycleEvents.SERVER_STARTING.register(JServerConfig::load);
        ServerLifecycleEvents.SERVER_STARTED.register(JServerEvents::finishLoading);
    }
}
