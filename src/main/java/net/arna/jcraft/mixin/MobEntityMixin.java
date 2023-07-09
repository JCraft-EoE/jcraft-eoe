package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

import static net.arna.jcraft.JCraft.ALLOW_MOB_EVOLVED_STANDS;
import static net.arna.jcraft.JCraft.CHANCE_MOB_SPAWNS_WITH_STAND;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Final
    @Shadow
    private DefaultedList<ItemStack> handItems;
    @Final
    @Shadow
    private DefaultedList<ItemStack> armorItems;

    private final List<Enchantment> jcraftArmorEnchants = List.of(Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.UNBREAKING);
    private final List<Item> jcraftHeadArmor = List.of(Items.AIR, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
    private final List<Item> jcraftChestArmor = List.of(Items.AIR, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
    private final List<Item> jcraftLegArmor = List.of(Items.AIR, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
    private final List<Item> jcraftFootArmor = List.of(Items.AIR, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    public void jcraft$mobEntityInit(EntityType<? extends MobEntity> entityType, World world, CallbackInfo info) {
        if (world.isClient) return;

        ServerWorld serverWorld = (ServerWorld) world;
        MobEntity mob = (MobEntity) (Object) this;

        NbtCompound nbt = ((IEntityDataSaver) mob).getPersistentData();

        if (nbt.contains("StandID")) return;
        EntityGroup group = mob.getGroup();

        if (group != EntityGroup.UNDEAD && group != EntityGroup.ILLAGER && !(mob instanceof EndermanEntity)) return;
        Random random = new Random();
        GameRules gameRules = serverWorld.getGameRules();

        if (100 - random.nextInt(0, 100) > gameRules.getInt(CHANCE_MOB_SPAWNS_WITH_STAND)) return;
        List<StandType> types = gameRules.getBoolean(ALLOW_MOB_EVOLVED_STANDS) ? StandType.getAllStandTypes() : StandType.getRegularStandTypes();
        StandType type = types.get(random.nextInt(types.size()));

        // Silver chariot users may spawn with anubis (25% chance)
        if (type == StandType.SILVER_CHARIOT && random.nextInt(5) == 4) {
            handItems.set(0, new ItemStack(JObjectRegistry.ANUBIS));
        }

        nbt.putInt("StandID", type.getId());

        EntityAttributeInstance followRange = mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(128.0);
        }

        EntityAttributeInstance movementSpeed = mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null && movementSpeed.getBaseValue() < 0.3) {
            movementSpeed.setBaseValue(0.3);
        }

        if (random.nextInt(0, 100) >= 90) {
            handItems.set(1, new ItemStack(JObjectRegistry.STANDARROW));
            mob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 100f);
        }

        Enchantment enchantment;
        ItemStack itemStack;

        int baseArmorLevel = random.nextInt(1, 6);
        int enchantsSize = jcraftArmorEnchants.size();

        // Randomize armor because stands tear through anything unarmored
        itemStack = new ItemStack(jcraftHeadArmor.get(baseArmorLevel + random.nextInt(-1, 1)));
        enchantment = jcraftArmorEnchants.get(random.nextInt(enchantsSize));
        itemStack.addEnchantment(enchantment, enchantment.getMaxLevel());
        armorItems.set(3, itemStack);

        itemStack = new ItemStack(jcraftChestArmor.get(baseArmorLevel + random.nextInt(-1, 1)));
        enchantment = jcraftArmorEnchants.get(random.nextInt(enchantsSize));
        itemStack.addEnchantment(enchantment, enchantment.getMaxLevel());
        armorItems.set(2, itemStack);

        itemStack = new ItemStack(jcraftLegArmor.get(baseArmorLevel + random.nextInt(-1, 1)));
        enchantment = jcraftArmorEnchants.get(random.nextInt(enchantsSize));
        itemStack.addEnchantment(enchantment, enchantment.getMaxLevel());
        armorItems.set(1, itemStack);

        itemStack = new ItemStack(jcraftFootArmor.get(baseArmorLevel + random.nextInt(-1, 1)));
        enchantment = jcraftArmorEnchants.get(random.nextInt(enchantsSize));
        itemStack.addEnchantment(enchantment, enchantment.getMaxLevel());
        armorItems.set(0, itemStack);
    }
}
