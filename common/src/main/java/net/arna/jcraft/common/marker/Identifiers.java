package net.arna.jcraft.common.marker;

import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;

public interface Identifiers {

    String MINECRAFT = ResourceLocation.DEFAULT_NAMESPACE;

    // Entity
    ResourceLocation POSITION = new ResourceLocation(MINECRAFT, "pos");
    ResourceLocation VELOCITY = new ResourceLocation(MINECRAFT, "vel");
    ResourceLocation PITCH = new ResourceLocation(MINECRAFT, "xrot");
    ResourceLocation YAW = new ResourceLocation(MINECRAFT, "yrot");
    ResourceLocation YAW_HEAD = new ResourceLocation(MINECRAFT, "yheadrot");
    ResourceLocation FALL_DISTANCE = new ResourceLocation(MINECRAFT, "fall_distance");
    ResourceLocation FIRE = new ResourceLocation(MINECRAFT, "fire");
    ResourceLocation AIR = new ResourceLocation(MINECRAFT, "air");
    ResourceLocation GROUNDED = new ResourceLocation(MINECRAFT, "grounded");
    ResourceLocation INVULNERABLE = new ResourceLocation(MINECRAFT, "invulnerable");
    ResourceLocation PORTAL_COOLDOWN = new ResourceLocation(MINECRAFT, "portal_cooldown");
    ResourceLocation UUID = new ResourceLocation(MINECRAFT, "uuid");
    ResourceLocation CUSTOM_NAME = new ResourceLocation(MINECRAFT, "custom_name");
    ResourceLocation CUSTOM_NAME_VISIBLE = new ResourceLocation(MINECRAFT, "custom_name_visible");
    ResourceLocation SILENT = new ResourceLocation(MINECRAFT, "silent");
    ResourceLocation NO_GRAVITY = new ResourceLocation(MINECRAFT, "no_gravity");
    ResourceLocation GLOWING = new ResourceLocation(MINECRAFT, "glowing");
    ResourceLocation TICKS_FROZEN = new ResourceLocation(MINECRAFT, "ticks_frozen");
    ResourceLocation TAGS = new ResourceLocation(MINECRAFT, "tags");
    ResourceLocation VEHICLE = new ResourceLocation(MINECRAFT, "vehicle");

    // Living Entity
    ResourceLocation HEALTH = new ResourceLocation(MINECRAFT, "health");
    ResourceLocation HURT_TIME = new ResourceLocation(MINECRAFT, "hurt_time");
    ResourceLocation DEATH_TIME = new ResourceLocation(MINECRAFT, "death_time");
    ResourceLocation ABSORPTION_AMOUNT = new ResourceLocation(MINECRAFT, "absorption_amount");
    ResourceLocation ATTRIBUTES = new ResourceLocation(MINECRAFT, "attributes");
    ResourceLocation ACTIVE_EFFECTS = new ResourceLocation(MINECRAFT, "active_effects");
    ResourceLocation SLEEPING_POSITION = new ResourceLocation(MINECRAFT, "sleeping_pos");

    // Mobs
    ResourceLocation CAN_PICKUP_LOOT = new ResourceLocation(MINECRAFT, "can_pickup_loot");
    ResourceLocation PERSISTENCE_REQUIRED = new ResourceLocation(MINECRAFT, "can_pickup_loot");
    ResourceLocation LEFT_HANDED_MOB = new ResourceLocation(MINECRAFT, "left_handed_mob");
    ResourceLocation NO_AI = new ResourceLocation(MINECRAFT, "no_ai");

    // Ageable Mobs
    ResourceLocation AGE = new ResourceLocation(MINECRAFT, "age");

    // Player
    ResourceLocation INVENTORY = new ResourceLocation(MINECRAFT, "player_inventory");
    ResourceLocation SELECTED_ITEM_SLOT = new ResourceLocation(MINECRAFT, "selected_item_slot");
    ResourceLocation XP_PROGRESS = new ResourceLocation(MINECRAFT, "xp_progress");
    ResourceLocation XP_LEVEL = new ResourceLocation(MINECRAFT, "xp_level");
    ResourceLocation XP_TOTAL = new ResourceLocation(MINECRAFT, "xp_total");
    ResourceLocation SCORE = new ResourceLocation(MINECRAFT, "score");
    ResourceLocation FOOD_DATA = new ResourceLocation(MINECRAFT, "food_data");
    ResourceLocation PLAYER_ABILITIES = new ResourceLocation(MINECRAFT, "player_abilities");
    ResourceLocation ENDER_CHEST = new ResourceLocation(MINECRAFT, "ender_chest");

    // Vampires
    ResourceLocation BLOOD_GAUGE = JCraft.id("blood_gauge");

    // Hamon Users
    ResourceLocation HAMON_CHARGE = JCraft.id("hamon_charge");
}
