package net.arna.jcraft.common.marker;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.spec.VampireSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.NbtUtils;
import net.arna.jcraft.common.util.TriConsumer;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

import static net.arna.jcraft.common.marker.Identifiers.*;

public interface Extractors {TriConsumer<ResourceLocation, Entity, CompoundTag> ENTITY = (id, entity, compoundTag) -> {
        if (id == null) {
            return;
        }
        if (id.equals(POSITION)) {
            final Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                NbtUtils.put(compoundTag, POSITION.toString(), new Vec3(vehicle.getX(), vehicle.getY(), vehicle.getZ()));
            } else {
                NbtUtils.put(compoundTag, POSITION.toString(), new Vec3(entity.getX(), entity.getY(), entity.getZ()));
            }
        } else if (id.equals(VELOCITY)) {
            NbtUtils.put(compoundTag, VELOCITY.toString(), entity.getDeltaMovement());
        } else if (id.equals(PITCH)) {
            compoundTag.putFloat(PITCH.toString(), entity.getXRot());
        } else if (id.equals(YAW)) {
            compoundTag.putFloat(YAW.toString(), entity.getYRot());
        } else if (id.equals(YAW_HEAD)) {
            compoundTag.putFloat(YAW_HEAD.toString(), entity.getYHeadRot());
        } else if (id.equals(FALL_DISTANCE)) {
            compoundTag.putFloat(FALL_DISTANCE.toString(), entity.fallDistance);
        } else if (id.equals(FIRE)) {
            compoundTag.putInt(FIRE.toString(), entity.getRemainingFireTicks());
        } else if (id.equals(AIR)) {
            compoundTag.putInt(AIR.toString(), entity.getAirSupply());
        } else if (id.equals(GROUNDED)) {
            compoundTag.putBoolean(GROUNDED.toString(), entity.onGround());
        } else if (id.equals(INVULNERABLE)) {
            compoundTag.putBoolean(INVULNERABLE.toString(), entity.isInvulnerable());
        } else if (id.equals(PORTAL_COOLDOWN)) {
            compoundTag.putInt(PORTAL_COOLDOWN.toString(), entity.getPortalCooldown());
        } else if (id.equals(UUID)) {
            compoundTag.putUUID(UUID.toString(), entity.getUUID());
        } else if (id.equals(CUSTOM_NAME)) {
            final Component component = entity.getCustomName();
            if (component != null) {
                compoundTag.putString(CUSTOM_NAME.toString(), Component.Serializer.toJson(component));
            }
        } else if (id.equals(CUSTOM_NAME_VISIBLE)) {
            compoundTag.putBoolean(CUSTOM_NAME_VISIBLE.toString(), entity.isCustomNameVisible());
        } else if (id.equals(SILENT)) {
            compoundTag.putBoolean(SILENT.toString(), entity.isSilent());
        } else if (id.equals(NO_GRAVITY)) {
            compoundTag.putBoolean(NO_GRAVITY.toString(), entity.isNoGravity());
        } else if (id.equals(GLOWING)) {
            compoundTag.putBoolean(GLOWING.toString(), entity.hasGlowingTag());
        } else if (id.equals(TICKS_FROZEN)) {
            compoundTag.putInt(TICKS_FROZEN.toString(), entity.getTicksFrozen());
        } else if (id.equals(TAGS)) {
            final ListTag listTag = new ListTag();
            for (final String string : entity.getTags()) {
                listTag.add(StringTag.valueOf(string));
            }
            compoundTag.put(TAGS.toString(), listTag);
        } else if (id.equals(VEHICLE) && entity.getVehicle() != null) {
            compoundTag.putUUID(VEHICLE.toString(), entity.getVehicle().getUUID());
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> LIVING_ENTITY = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        if (id.equals(HEALTH)) {
            compoundTag.putFloat(HEALTH.toString(), livingEntity.getHealth());
        } else if (id.equals(HURT_TIME)) {
            compoundTag.putInt(HURT_TIME.toString(), livingEntity.hurtTime);
        } else if (id.equals(DEATH_TIME)) {
            compoundTag.putInt(DEATH_TIME.toString(), livingEntity.deathTime);
        } else if (id.equals(ABSORPTION_AMOUNT)) {
            compoundTag.putFloat(ABSORPTION_AMOUNT.toString(), livingEntity.getAbsorptionAmount());
        } else if (id.equals(ATTRIBUTES)) {
            compoundTag.put(ATTRIBUTES.toString(), livingEntity.getAttributes().save());
        } else if (id.equals(ACTIVE_EFFECTS)) { // TODO only save certain effects?
            final ListTag listTag = new ListTag();
            for (final MobEffectInstance mobEffectInstance : livingEntity.getActiveEffects()) {
                listTag.add(mobEffectInstance.save(new CompoundTag()));
            }
            compoundTag.put(ACTIVE_EFFECTS.toString(), listTag);
        } else if (id.equals(SLEEPING_POSITION) && livingEntity.getSleepingPos().isPresent()) {
            NbtUtils.put(compoundTag, SLEEPING_POSITION.toString(), livingEntity.getSleepingPos().get());
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> PLAYER = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final Player player)) {
            return;
        }
        if (id.equals(INVENTORY)) {
            compoundTag.put(INVENTORY.toString(), player.getInventory().save(new ListTag()));
        } else if (id.equals(SELECTED_ITEM_SLOT)) {
            compoundTag.putInt(SELECTED_ITEM_SLOT.toString(), player.getInventory().selected);
        } else if (id.equals(XP_PROGRESS)) {
            compoundTag.putFloat(XP_PROGRESS.toString(), player.experienceProgress);
        } else if (id.equals(XP_LEVEL)) {
            compoundTag.putInt(XP_LEVEL.toString(), player.experienceLevel);
        } else if (id.equals(XP_TOTAL)) {
            compoundTag.putInt(XP_TOTAL.toString(), player.totalExperience);
        }
        // enchantment seed cannot be set without mixin
        else if (id.equals(SCORE)) {
            compoundTag.putInt(SCORE.toString(), player.getScore());
        }
        // TODO only save specific food data stuff?
        else if (id.equals(FOOD_DATA)) {
            player.getFoodData().addAdditionalSaveData(compoundTag);
        }
        // TODO only save specific abilities?
        else if (id.equals(PLAYER_ABILITIES)) {
            player.getAbilities().addSaveData(compoundTag);
        } else if (id.equals(ENDER_CHEST)) {
            compoundTag.put(ENDER_CHEST.toString(), player.getEnderChestInventory().createTag());
        }
        // shoulder entity setting is protected
        // last death location is a bit complicated
    };

    // TODO ServerPlayer

    TriConsumer<ResourceLocation, Entity, CompoundTag> MOB = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final Mob mob)) {
            return;
        }
        if (id.equals(CAN_PICKUP_LOOT)) {
            compoundTag.putBoolean(CAN_PICKUP_LOOT.toString(), mob.canPickUpLoot());
        } else if (id.equals(PERSISTENCE_REQUIRED)) {
            compoundTag.putBoolean(PERSISTENCE_REQUIRED.toString(), mob.isPersistenceRequired());
        }
        // armor and hand items are missing setters
        // armor and hand drop chances cannot be set without mixin
        // TODO leash info
        else if (id.equals(LEFT_HANDED_MOB)) {
            compoundTag.putBoolean(LEFT_HANDED_MOB.toString(), mob.isLeftHanded());
        }
        // TODO loot table info
        else if (id.equals(NO_AI)) {
            compoundTag.putBoolean(NO_AI.toString(), mob.isLeftHanded());
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> AGEABLE_MOB = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final AgeableMob ageableMob)) {
            return;
        }
        if (id.equals(AGE)) {
            compoundTag.putInt(AGE.toString(), ageableMob.getAge());
        }
        // forced age cannot be set without mixin
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> VAMPIRE = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        final CommonVampireComponent vampire = JComponentPlatformUtils.getVampirism(livingEntity);
        if (vampire != null && vampire.isVampire() && id.equals(BLOOD_GAUGE)) {
            compoundTag.putFloat(BLOOD_GAUGE.toString(), vampire.getBlood());
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> HAMON = (id, entity, compoundTag) -> {
        if (id == null || !(entity instanceof final LivingEntity livingEntity)) {
            return;
        }
        final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(livingEntity);
        if (hamon != null && JUtils.getSpec(livingEntity) instanceof HamonSpec && id.equals(HAMON_CHARGE)) {
            compoundTag.putFloat(HAMON_CHARGE.toString(), hamon.getHamonCharge());
        }
    };

    TriConsumer<ResourceLocation, Entity, CompoundTag> ALL = (id, entity, compoundTag) -> {
        ENTITY.accept(id, entity, compoundTag);
        LIVING_ENTITY.accept(id, entity, compoundTag);
        PLAYER.accept(id, entity, compoundTag);
        MOB.accept(id, entity, compoundTag);
        AGEABLE_MOB.accept(id, entity, compoundTag);
        VAMPIRE.accept(id, entity, compoundTag);
        HAMON.accept(id, entity, compoundTag);
    };
}