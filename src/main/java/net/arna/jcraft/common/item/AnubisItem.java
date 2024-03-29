package net.arna.jcraft.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnubisItem extends Item {
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public AnubisItem(Settings settings) {
        super(settings);
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 6.0, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.4, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("jcraft.anubis.namedesc"));
        tooltip.add(Text.translatable("jcraft.anubis.bloodthirstdesc"));
        tooltip.add(Text.translatable("jcraft.anubis.removaldesc"));
        tooltip.add(Text.translatable("jcraft.anubis.desc"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack item = user.getStackInHand(hand);
        StandEntity<?, ?> stand = JUtils.getStand(user);

        if (user.isSneaking() || world.isClient || (stand != null && stand.blocking)) return TypedActionResult.fail(item);

        JUtils.serverPlaySound(JSoundRegistry.ANUBIS_SHEATHE, (ServerWorld) world, user.getPos());
        user.setStackInHand(hand, new ItemStack(JObjectRegistry.ANUBISSHEATHED));

        return TypedActionResult.success(item);
    }

    public static void handleAnubisEffects(int timeSinceAttack, PlayerEntity player) {
        // If was in battle within last 5 minutes, apply haste 1
        if (timeSinceAttack > -6000) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20, 0, true, false));
        } else {
            // If wasn't in battle for 10 minutes, apply mining fatigue 1
            if (timeSinceAttack < -12000) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 20, 0, true, false));
                // If wasn't in battle for 15 minutes, apply weakness 1
                if (timeSinceAttack < -18000) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 0, true, false));
                    // If wasn't in battle for 20 minutes, apply nausea 1
                    if (timeSinceAttack < -24000) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20, 0, true, false));
                        // If wasn't in battle for 25 minutes, apply slowness 1
                        if (timeSinceAttack < -30000) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 0, true, false));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (world.isClient) return;
        if (entity instanceof PlayerEntity player) // Bloodlust
            handleAnubisEffects(player.getLastAttackTime() - player.age, player);
    }
}
