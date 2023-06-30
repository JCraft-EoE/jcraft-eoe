package net.arna.jcraft.common.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ISpec;
import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SheathedAnubisItem extends AnubisItem {
    boolean warned = false; // This warning resets every time you join the world

    public SheathedAnubisItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.of("§9The sword/stand named after the Egyptian god of death."));
        tooltip.add(Text.of("§eBloodthirsty. §9Fuels itself on any and all violence."));
        tooltip.add(Text.of("§9Can used to §eblock."));
        tooltip.add(Text.of("§eHard to get rid of."));
        tooltip.add(Text.of("§9Can §eunsheathed §9with §eCrouch + RMB."));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.fail(itemStack);
        if (!user.isSneaking()) user.setCurrentHand(hand);
        else {
            JCraftSpec spec = JCraftUtils.getSpec(user);
            if (spec instanceof AnubisSpec)
                user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBIS));
            else if (warned) {
                NbtCompound data = ((IEntityDataSaver)user).getPersistentData();
                data.putInt("SpecID", 2);
                JCraftUtils.assignSpec(user, data, (ISpec)user);
                user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBIS));
                warned = false;
            } else {
                user.sendMessage(Text.translatable("jcraft.anubis.unsheathe"));
                warned = true;
            }
        }
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient) return;
        if (entity instanceof PlayerEntity player) // Bloodlust
            handleAnubisEffects(player.getLastAttackTime() - player.age, player);
    }
}
