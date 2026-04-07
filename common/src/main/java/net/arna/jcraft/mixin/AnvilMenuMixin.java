package net.arna.jcraft.mixin;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.api.registry.JEnchantmentRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.enchantments.CinderellasKissEnchantment;
import net.arna.jcraft.common.item.CosplayItem;
import net.arna.jcraft.common.item.StandDiscItem;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    @Shadow @Final private DataSlot cost;
    @Shadow private String itemName;

    @Shadow
    private int repairItemCountCost;

    private AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult()V", at = @At("RETURN"))
    private void jcraft$injectAnvil(final CallbackInfo ci) {
        ItemStack item1 = inputSlots.getItem(0);
        ItemStack item2 = inputSlots.getItem(1);
        if (item1.is(JItemRegistry.CINDERELLA_MASK.get()) && item2.is(Items.ENCHANTED_BOOK)) {
            jcraft$enchantMask(item1, item2);
        }
        else if (item1.is(JItemRegistry.STAND_DISC.get()) && item2.is(JItemRegistry.CINDERELLA_MASK.get())) {
            jcraft$switchSkin(item1, item2);
        }
        else if (item1.is(JTagRegistry.COSPLAY) || item2.is(JTagRegistry.COSPLAY)) {
            jcraft$upgradeCosplay(item1, item2);
        }
    }

    @Unique
    private void jcraft$enchantMask(final ItemStack mask, final ItemStack book) {
        final var bookEnchantments = EnchantmentHelper.getEnchantments(book);
        if (!bookEnchantments.containsKey(JEnchantmentRegistry.CINDERELLAS_KISS.get())) {
            return;
        }
        // calculate the base cost
        final var maskEnchantments = EnchantmentHelper.getEnchantments(mask);
        final Integer enchantedLevel = maskEnchantments.computeIfAbsent(JEnchantmentRegistry.CINDERELLAS_KISS.get(), e -> 0);
        final Integer enchantLevel = bookEnchantments.get(JEnchantmentRegistry.CINDERELLAS_KISS.get());
        int c = enchantedLevel + enchantLevel;
        if (c <= 0) {
            return;
        }
        // prepare the result
        final int newEnchantedLevel = enchantedLevel.equals(enchantLevel) ? enchantedLevel + 1 : Math.max(enchantedLevel, enchantLevel);
        final ItemStack enchantedMask = new ItemStack(JItemRegistry.CINDERELLA_MASK.get());
        EnchantmentHelper.setEnchantments(Map.of(JEnchantmentRegistry.CINDERELLAS_KISS.get(), newEnchantedLevel), enchantedMask);
        // check for custom item name
        if (itemName != null && !Util.isBlank(itemName)) {
            if (!itemName.equals(mask.getHoverName().getString())) {
                c += 1;
                enchantedMask.setHoverName(Component.literal(this.itemName));
            }
        }
        else if (mask.hasCustomHoverName()) {
            c += 1;
            enchantedMask.resetHoverName();
        }
        // add repair costs
        c += mask.getBaseRepairCost() + book.getBaseRepairCost();
        // we don't limit the costs though, as other mods who disable that mixin the anvil code more directly and won't come here
        // calculate new repair cost
        enchantedMask.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(Math.max(mask.getBaseRepairCost(), book.getBaseRepairCost())));
        // finalize product
        resultSlots.setItem(0, enchantedMask);
        cost.set(c);
        broadcastChanges();
    }

    @Unique
    private void jcraft$switchSkin(final ItemStack disc, final ItemStack mask) {
        // If the disc is empty, return.
        StandType standType = StandDiscItem.getStandType(disc);
        if (standType == null)
            return;
        // The CK level must be at most equal to the amount of skins the stand has
        // and must not be the same as the disc's current skin.
        int level = CinderellasKissEnchantment.getCKLevel(mask);
        if (level >= standType.getData().getInfo().getSkinCount() || level == StandDiscItem.getSkin(disc)) {
            return;
        }
        // hack to make it not consume multiple Cinderella masks at once
        repairItemCountCost = 1;
        // prepare the result
        final ItemStack newDisc = StandDiscItem.createDiscStack(standType, level);
        int c = 5;
        // check for custom item name
        if (itemName != null && !Util.isBlank(itemName)) {
            if (!itemName.equals(disc.getHoverName().getString())) {
                c += 1;
                newDisc.setHoverName(Component.literal(this.itemName));
            }
        }
        else if (mask.hasCustomHoverName()) {
            c += 1;
            newDisc.resetHoverName();
        }
        // finalize product
        resultSlots.setItem(0, newDisc);
        cost.set(c);
    }

    @Unique
    private void jcraft$upgradeCosplay(final ItemStack item1, final ItemStack item2) {
        // both items must be armor
        if (!(item1.getItem() instanceof final ArmorItem armor1) || !(item2.getItem() instanceof final ArmorItem armor2)) {
            return;
        }
        // the slot must be the same
        if (armor1.getType() != armor2.getType()) {
            return;
        }
        // if the items are the same, the anvil should already have calculated a result
        // if they are of the same tier only, no change should be possible
        if (armor1.getMaterial() == armor2.getMaterial()) {
            return;
        }
        final ItemStack cosplay;
        final ItemStack upgrade;
        // left side takes precedence if both are cosplay
        if (!item1.is(JTagRegistry.COSPLAY)) {
            cosplay = item2;
            upgrade = item1;
        }
        else {
            cosplay = item1;
            upgrade = item2;
        }
        final ArmorMaterial upgradeTier = ((ArmorItem)upgrade.getItem()).getMaterial();
        final CosplayItem<?> cosplayItem = CosplayItem.find(cosplay.getItem());
        if (cosplayItem == null) { // shouldn't happen because of the selection in jcraft$injectAnvil
            return;
        }
        final RegistrySupplier<? extends ArmorItem> upgradeItem = cosplayItem.get(upgradeTier);
        if (upgradeItem == null) { // means we have no cosplay item tier for the material
            return;
        }
        final ItemStack result = new ItemStack(upgradeItem.get());
        int c = 0;
        // this is not cosplay/upgrade but item1/item2 on purpose!!
        final var baseEnchantments = EnchantmentHelper.getEnchantments(item1);
        final var upgradeEnchantments = EnchantmentHelper.getEnchantments(item2);
        // calculate result enchantments
        final var resultEnchantments = EnchantmentHelper.getEnchantments(result);
        for (final Enchantment upgradeEnchantment : upgradeEnchantments.keySet()) {
            // don't copy incompatible unless in creative
            if (!player.getAbilities().instabuild) {
                boolean skip = false;
                for (final Enchantment baseEnchantment : baseEnchantments.keySet()) {
                    if (baseEnchantment != upgradeEnchantment && !upgradeEnchantment.isCompatibleWith(baseEnchantment)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }
            final int baseLevel = baseEnchantments.getOrDefault(upgradeEnchantment, 0);
            final int upgradeLevel = upgradeEnchantments.get(upgradeEnchantment);
            int resultLevel = baseLevel == upgradeLevel ? upgradeLevel + 1 : Math.max(baseLevel, upgradeLevel);
            if (resultLevel > upgradeEnchantment.getMaxLevel()) {
                resultLevel = upgradeEnchantment.getMaxLevel();
            }
            resultEnchantments.put(upgradeEnchantment, resultLevel);
        }
        // copy remaining base enchantments
        for (final var baseEntry : baseEnchantments.entrySet()) {
            if (!upgradeEnchantments.containsKey(baseEntry.getKey())) {
                resultEnchantments.put(baseEntry.getKey(), baseEntry.getValue());
            }
        }
        // increase cost for name changes
        if (itemName != null && !Util.isBlank(itemName)) {
            if (!itemName.equals(item1.getHoverName().getString())) {
                c += 1;
                result.setHoverName(Component.literal(this.itemName));
            }
        } else if (item1.hasCustomHoverName()) {
            c += 1;
        }
        // finalize product
        resultSlots.setItem(0, result);
        cost.set(1);
        // cost.set(c);
    }

}
