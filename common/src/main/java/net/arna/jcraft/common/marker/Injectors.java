package net.arna.jcraft.common.marker;

import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.common.util.NbtUtils;
import net.arna.jcraft.common.util.TriConsumer;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import static net.arna.jcraft.common.marker.Identifiers.*;

public interface Injectors {

    // We only land here if a property should be included
    // so if we *don't* find the id's tag we must assume it
    // wasn't there and hence must be deleted.

    TriConsumer<ResourceLocation, Entity, CompoundTag> ENTITY = (id, entity, compoundTag) -> {
        if (id == null) {
            return;
        }
        if (id.equals(POSITION) && compoundTag.contains(POSITION.toString()) && compoundTag.contains(PITCH.toString()) && compoundTag.contains(YAW.toString()) && compoundTag.contains(YAW_HEAD.toString())) {
            final Vec3 pos = NbtUtils.getVec3(compoundTag, POSITION.toString());
            final float pitch = compoundTag.getFloat(PITCH.toString());
            final float yaw = compoundTag.getFloat(YAW.toString());
            final float yawHead = compoundTag.getFloat(YAW_HEAD.toString());
            if (entity instanceof final ServerPlayer serverPlayer) {
                // use teleportTo with proper rotation handling
                serverPlayer.teleportTo(serverPlayer.serverLevel(), pos.x(), pos.y(), pos.z(),
                        EnumSet.noneOf(RelativeMovement.class), yaw, pitch);
                // force update head rotation for other players
                serverPlayer.setYHeadRot(yawHead);
                serverPlayer.connection.send(new ClientboundRotateHeadPacket(serverPlayer, (byte) ((int) (yaw * 256.0F / 360.0F))));
                serverPlayer.connection.send(new ClientboundTeleportEntityPacket(serverPlayer));
            } else {
                entity.teleportTo(pos.x(), pos.y(), pos.z());
                entity.setYRot(yaw);
                entity.setXRot(pitch);
                entity.setYHeadRot(yawHead);
                entity.setYBodyRot(yaw);
                entity.yRotO = yaw;
                entity.xRotO = pitch;
                if (entity instanceof final LivingEntity livingEntity) {
                    livingEntity.yHeadRotO = yawHead;
                    livingEntity.yBodyRotO = yaw;
                }
            }
            if (compoundTag.contains(VEHICLE.toString())) {
                final var vehicleUuid = compoundTag.getUUID(VEHICLE.toString());
                final Entity vehicle = ((ServerLevel)entity.level()).getEntity(vehicleUuid);
                if (vehicle != null) {
                    entity.startRiding(vehicle);
                }
            }
        } else if (id.equals(VELOCITY)) {
            if (compoundTag.contains(VELOCITY.toString())) {
                entity.setDeltaMovement(NbtUtils.getVec3(compoundTag, VELOCITY.toString()));
            } else {
                entity.setDeltaMovement(0d, 0d, 0d);
            }
            if (entity instanceof final ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        } else if (id.equals(FALL_DISTANCE)) {
            if (compoundTag.contains(FALL_DISTANCE.toString())) {
                entity.fallDistance = compoundTag.getFloat(FALL_DISTANCE.toString());
            } else {
                entity.fallDistance = 0f;
            }
        } else if (id.equals(FIRE)) {
            if (compoundTag.contains(FIRE.toString())) {
                entity.setRemainingFireTicks(compoundTag.getInt(FIRE.toString()));
            } else {
                entity.setRemainingFireTicks(0);
            }
        } else if (id.equals(AIR)) {
            if (compoundTag.contains(AIR.toString())) {
                entity.setAirSupply(compoundTag.getInt(AIR.toString()));
            } else {
                entity.setAirSupply(20);
            }
        } else if (id.equals(GROUNDED)) {
            if (compoundTag.contains(GROUNDED.toString())) {
                entity.setOnGround(compoundTag.getBoolean(GROUNDED.toString()));
            } else {
                entity.setOnGround(true);
            }
        } else if (id.equals(INVULNERABLE)) {
            if (compoundTag.contains(INVULNERABLE.toString())) {
                entity.setInvulnerable(compoundTag.getBoolean(INVULNERABLE.toString()));
            } else {
                entity.setOnGround(true);
            }
        } else if (id.equals(PORTAL_COOLDOWN)) {
            if (compoundTag.contains(PORTAL_COOLDOWN.toString())) {
                entity.setPortalCooldown(compoundTag.getInt(PORTAL_COOLDOWN.toString()));
            } else {
                entity.setPortalCooldown(0);
            }
        } else if (id.equals(UUID) && compoundTag.contains(UUID.toString())) {
            entity.setUUID(compoundTag.getUUID(UUID.toString()));
        } else if (id.equals(CUSTOM_NAME)) {
            final String name = compoundTag.getString(CUSTOM_NAME.toString());
            if (name.isEmpty()) {
                entity.setCustomName(null);
            } else {
                entity.setCustomName(Component.Serializer.fromJson(name));
            }
        } else if (id.equals(CUSTOM_NAME_VISIBLE)) {
            if (compoundTag.contains(CUSTOM_NAME_VISIBLE.toString())) {
                entity.setCustomNameVisible(compoundTag.getBoolean(CUSTOM_NAME_VISIBLE.toString()));
            } else {
                entity.setCustomNameVisible(true);
            }
        } else if (id.equals(SILENT)) {
            if (compoundTag.contains(SILENT.toString())) {
                entity.setSilent(compoundTag.getBoolean(SILENT.toString()));
            } else {
                entity.setSilent(false);
            }
        } else if (id.equals(NO_GRAVITY)) {
            if (compoundTag.contains(NO_GRAVITY.toString())) {
                entity.setNoGravity(compoundTag.getBoolean(NO_GRAVITY.toString()));
            } else {
                entity.setNoGravity(false);
            }
        } else if (id.equals(GLOWING)) {
            if (compoundTag.contains(GLOWING.toString())) {
                entity.setGlowingTag(compoundTag.getBoolean(GLOWING.toString()));
            } else {
                entity.setGlowingTag(false);
            }
        } else if (id.equals(TICKS_FROZEN)) {
            if (compoundTag.contains(TICKS_FROZEN.toString())) {
                entity.setTicksFrozen(compoundTag.getInt(TICKS_FROZEN.toString()));
            } else {
                entity.setTicksFrozen(0);
            }
        } else if (id.equals(TAGS)) {
            entity.getTags().clear();
            if (compoundTag.contains(TAGS.toString(), Tag.TAG_LIST)) {
                final ListTag list = compoundTag.getList(TAGS.toString(), Tag.TAG_STRING);
                for (int i = 0; i < list.size(); i++) {
                    entity.getTags().add(list.getString(i));
                }
            }
        }
        // TODO passengers?
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> LIVING_ENTITY = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        if (id.equals(HEALTH)) {
            if (compoundTag.contains(HEALTH.toString())) {
                livingEntity.setHealth(compoundTag.getFloat(HEALTH.toString()));
            } else {
                livingEntity.setHealth(livingEntity.getMaxHealth());
            }
        } else if (id.equals(HURT_TIME) && compoundTag.contains(HURT_TIME.toString())) {
            livingEntity.hurtTime = compoundTag.getInt(HURT_TIME.toString());
        } else if (id.equals(DEATH_TIME) && compoundTag.contains(DEATH_TIME.toString())) {
            livingEntity.deathTime = compoundTag.getInt(DEATH_TIME.toString());
        } else if (id.equals(ABSORPTION_AMOUNT)) {
            if (compoundTag.contains(ABSORPTION_AMOUNT.toString())) {
                livingEntity.setAbsorptionAmount(compoundTag.getFloat(ABSORPTION_AMOUNT.toString()));
            } else {
                livingEntity.setAbsorptionAmount(0f);
            }
        } else if (id.equals(ATTRIBUTES) && compoundTag.contains(ATTRIBUTES.toString(), Tag.TAG_LIST)) {
            livingEntity.getAttributes().load(compoundTag.getList(ATTRIBUTES.toString(), Tag.TAG_COMPOUND));
        } else if (id.equals(ACTIVE_EFFECTS)) { // TODO only load certain effects?
            livingEntity.getActiveEffectsMap().clear();
            if (compoundTag.contains(ACTIVE_EFFECTS.toString())) {
                final ListTag list = compoundTag.getList(ACTIVE_EFFECTS.toString(), Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); ++i) {
                    final CompoundTag tag = list.getCompound(i);
                    final MobEffectInstance mobEffectInstance = MobEffectInstance.load(tag);
                    if (mobEffectInstance == null) {
                        continue;
                    }
                    livingEntity.getActiveEffectsMap().put(mobEffectInstance.getEffect(), mobEffectInstance);
                }
            }
        } else if (id.equals(SLEEPING_POSITION) && compoundTag.contains(SLEEPING_POSITION.toString())) {
            livingEntity.setSleepingPos(new BlockPos(NbtUtils.getVec3i(compoundTag, SLEEPING_POSITION.toString())));
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> PLAYER = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final Player player)) {
            return;
        }
        if (id.equals(INVENTORY)) {
            player.getInventory().clearContent();
            if (compoundTag.contains(INVENTORY.toString())) {
                player.getInventory().load(compoundTag.getList(INVENTORY.toString(), Tag.TAG_COMPOUND));
            }
        } else if (id.equals(SELECTED_ITEM_SLOT)) {
            if (compoundTag.contains(SELECTED_ITEM_SLOT.toString())) {
                player.getInventory().selected = compoundTag.getInt(SELECTED_ITEM_SLOT.toString());
            } else {
                player.getInventory().selected = 0;
            }
        } else if (id.equals(XP_PROGRESS)) {
            if (compoundTag.contains(XP_PROGRESS.toString())) {
                player.experienceProgress = compoundTag.getFloat(XP_PROGRESS.toString());
            } else {
                player.experienceProgress = 0f;
            }
        } else if (id.equals(XP_LEVEL)) {
            if (compoundTag.contains(XP_LEVEL.toString())) {
                player.experienceLevel = compoundTag.getInt(XP_LEVEL.toString());
            } else {
                player.experienceLevel = 0;
            }
        } else if (id.equals(XP_TOTAL)) {
            if (compoundTag.contains(XP_TOTAL.toString())) {
                player.totalExperience = compoundTag.getInt(XP_TOTAL.toString());
            } else {
                player.totalExperience = 0;
            }
        } else if (id.equals(SCORE)) {
            if (compoundTag.contains(SCORE.toString())) {
                player.setScore(compoundTag.getInt(SCORE.toString()));
            } else {
                player.setScore(0);
            }
        } else if (id.equals(FOOD_DATA)) {
            player.getFoodData().readAdditionalSaveData(compoundTag);
        } else if (id.equals(PLAYER_ABILITIES)) {
            player.getAbilities().loadSaveData(compoundTag);
        } else if (id.equals(ENDER_CHEST)) {
            player.getEnderChestInventory().fromTag(compoundTag.getList(ENDER_CHEST.toString(), Tag.TAG_COMPOUND));
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> MOB = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final Mob mob)) {
            return;
        }
        if (id.equals(CAN_PICKUP_LOOT)) {
            if (compoundTag.contains(CAN_PICKUP_LOOT.toString())) {
                mob.setCanPickUpLoot(compoundTag.getBoolean(CAN_PICKUP_LOOT.toString()));
            } else {
                mob.setCanPickUpLoot(false);
            }
        } else if (id.equals(PERSISTENCE_REQUIRED)) {
            if (compoundTag.contains(PERSISTENCE_REQUIRED.toString()) && compoundTag.getBoolean(PERSISTENCE_REQUIRED.toString())) {
                mob.setPersistenceRequired();
            }
        } else if (id.equals(LEFT_HANDED_MOB) && compoundTag.contains(LEFT_HANDED_MOB.toString())) {
            mob.setLeftHanded(compoundTag.getBoolean(LEFT_HANDED_MOB.toString()));
        } else if (id.equals(NO_AI)) {
            if (compoundTag.contains(NO_AI.toString())) {
                mob.setNoAi(compoundTag.getBoolean(NO_AI.toString()));
            }
            else {
                mob.setNoAi(false);
            }
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> AGABLE_MOB = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final AgeableMob ageableMob)) {
            return;
        }
        if (id.equals(AGE)) {
            if (compoundTag.contains(AGE.toString())) {
                ageableMob.setAge(compoundTag.getInt(AGE.toString()));
            }
            else {
                ageableMob.setAge(0);
            }
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> VAMPIRE = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        final CommonVampireComponent vampire = JComponentPlatformUtils.getVampirism(livingEntity);
        if (vampire != null && vampire.isVampire() && id.equals(BLOOD_GAUGE)) {
            if (compoundTag.contains(BLOOD_GAUGE.toString())) {
                vampire.setBlood(compoundTag.getFloat(BLOOD_GAUGE.toString()));
            }
            else {
                vampire.resetBlood();
            }
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> HAMON = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(livingEntity);
        if (hamon != null && id.equals(HAMON_CHARGE)) {
            if (compoundTag.contains(HAMON_CHARGE.toString())) {
                hamon.setHamonCharge(compoundTag.getFloat(HAMON_CHARGE.toString()));
            }
            else {
                hamon.resetHamonCharge();
            }
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> ALL = (id, entity, compoundTag) -> {
        ENTITY.accept(id, entity, compoundTag);
        LIVING_ENTITY.accept(id, entity, compoundTag);
        PLAYER.accept(id, entity, compoundTag);
        MOB.accept(id, entity, compoundTag);
        AGABLE_MOB.accept(id, entity, compoundTag);
        VAMPIRE.accept(id, entity, compoundTag);
        HAMON.accept(id, entity, compoundTag);
    };

}
